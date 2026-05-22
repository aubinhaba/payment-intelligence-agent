terraform {
  required_version = ">= 1.9"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "payment-intelligence-agent"
      Environment = "dev"
      ManagedBy   = "terraform"
    }
  }
}

data "aws_caller_identity" "current" {}
data "aws_availability_zones" "available" { state = "available" }

locals {
  account_id = data.aws_caller_identity.current.account_id
  azs        = slice(data.aws_availability_zones.available.names, 0, 2)

  core_image_url      = "${local.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/pia-dev-pia-core:${var.image_tag}"
  simulator_image_url = "${local.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/pia-dev-pia-simulator:${var.image_tag}"
}

# ── KMS — CloudWatch log encryption ──────────────────────────────────────────

module "kms" {
  source = "../../modules/kms"

  environment = "dev"
  app_name    = "pia"
}

# ── Networking ────────────────────────────────────────────────────────────────

module "networking" {
  source = "../../modules/networking"

  environment        = "dev"
  app_name           = "pia"
  availability_zones = local.azs
}

# ── ECR ───────────────────────────────────────────────────────────────────────

module "ecr" {
  source = "../../modules/ecr"

  environment      = "dev"
  app_name         = "pia"
  repository_names = ["pia-core", "pia-simulator"]
}

# ── SQS ───────────────────────────────────────────────────────────────────────

module "sqs" {
  source = "../../modules/sqs"

  environment    = "dev"
  app_name       = "pia"
  queue_basename = "payment-events"
  kms_key_id     = module.kms.kms_key_id
}

# Dedicated queue feeding AnalyzeAndReportUseCase (Claude + report persistence).
# See docs/adr/0011-async-agent-analysis-via-sqs.md
module "sqs_anomaly_analysis" {
  source = "../../modules/sqs"

  environment    = "dev"
  app_name       = "pia"
  queue_basename = "anomaly-analysis"
  kms_key_id     = module.kms.kms_key_id

  # Claude calls can be slow (multi-iteration tool loop); raise the visibility
  # timeout to avoid duplicate analyses while a message is being processed.
  visibility_timeout_seconds = var.anomaly_analysis_visibility_timeout_seconds
  max_receive_count          = var.anomaly_analysis_max_receive_count
}

# ── DynamoDB ──────────────────────────────────────────────────────────────────

module "dynamodb" {
  source = "../../modules/dynamodb"

  environment = "dev"
  app_name    = "pia"
}

# ── S3 ────────────────────────────────────────────────────────────────────────

module "s3" {
  source = "../../modules/s3"

  environment = "dev"
  app_name    = "pia"
  account_id  = local.account_id
}

# ── SSM Parameters ────────────────────────────────────────────────────────────

module "ssm" {
  source = "../../modules/ssm-parameters"

  environment = "dev"
  app_name    = "pia"
  kms_key_id  = module.kms.kms_key_id
}

# ── IAM ───────────────────────────────────────────────────────────────────────

module "iam" {
  source = "../../modules/iam"

  environment           = "dev"
  app_name              = "pia"
  dynamodb_table_arn    = module.dynamodb.table_arn
  sqs_queue_arn         = module.sqs.queue_arn
  sqs_dlq_arn           = module.sqs.dlq_arn
  s3_reports_bucket_arn = module.s3.reports_bucket_arn
  ecr_repository_arns   = module.ecr.repository_arns
  ssm_path_prefix       = "/pia/dev"

  extra_sqs_queue_arns = [
    module.sqs_anomaly_analysis.queue_arn,
    module.sqs_anomaly_analysis.dlq_arn,
  ]
}

# ── CloudWatch ────────────────────────────────────────────────────────────────

module "cloudwatch" {
  source = "../../modules/cloudwatch"

  environment        = "dev"
  app_name           = "pia"
  metrics_namespace  = "PIA"
  log_retention_days = 7
  kms_key_id         = module.kms.kms_key_id
  alert_email        = var.alert_email
  dlq_name           = module.sqs.dlq_name

  additional_dlq_names = [
    module.sqs_anomaly_analysis.dlq_name,
  ]

  claude_error_rate_threshold = 10
  anomaly_min_detection_rate  = 5
}

# ── ALB ───────────────────────────────────────────────────────────────────────

module "alb" {
  source = "../../modules/alb"

  environment       = "dev"
  app_name          = "pia"
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids
  security_group_id = module.networking.sg_alb_id
}

# ── ECS Cluster ───────────────────────────────────────────────────────────────

module "ecs_cluster" {
  source = "../../modules/ecs-cluster"

  environment = "dev"
  app_name    = "pia"
}

# ── ECS Service: pia-core ────────────────────────────────────────────────────

module "pia_core" {
  source = "../../modules/ecs-service"

  environment        = "dev"
  app_name           = "pia"
  service_name       = "pia-core"
  cluster_id         = module.ecs_cluster.cluster_id
  task_cpu           = 512
  task_memory        = 1024
  container_port     = 8080
  ecr_image_url      = local.core_image_url
  execution_role_arn = module.iam.execution_role_arn
  task_role_arn      = module.iam.task_role_arn
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = module.networking.sg_ecs_core_id
  target_group_arn   = module.alb.core_target_group_arn
  desired_count      = 0
  log_group_name     = "/pia/dev/app"
  region             = var.aws_region

  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE", value = "dev" },
    { name = "AWS_REGION", value = var.aws_region },
    { name = "DYNAMODB_TABLE_NAME", value = module.dynamodb.table_name },
    { name = "SQS_QUEUE_URL", value = module.sqs.queue_url },
    { name = "PIA_SQS_PAYMENT_EVENTS_QUEUE", value = module.sqs.queue_name },
    { name = "PIA_SQS_PAYMENT_EVENTS_DLQ", value = module.sqs.dlq_name },
    { name = "PIA_SQS_ANOMALY_ANALYSIS_QUEUE", value = module.sqs_anomaly_analysis.queue_name },
    { name = "PIA_SQS_ANOMALY_ANALYSIS_DLQ", value = module.sqs_anomaly_analysis.dlq_name },
    { name = "S3_REPORTS_BUCKET", value = module.s3.reports_bucket_name },
  ]

  secrets = [
    { name = "CLAUDE_API_KEY", valueFrom = module.ssm.claude_api_key_arn },
  ]
}

# ── ECS Service: pia-simulator ────────────────────────────────────────────────

module "pia_simulator" {
  source = "../../modules/ecs-service"

  environment        = "dev"
  app_name           = "pia"
  service_name       = "pia-simulator"
  cluster_id         = module.ecs_cluster.cluster_id
  task_cpu           = 256
  task_memory        = 512
  container_port     = 0
  ecr_image_url      = local.simulator_image_url
  execution_role_arn = module.iam.execution_role_arn
  task_role_arn      = module.iam.task_role_arn
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = module.networking.sg_ecs_simulator_id
  desired_count      = 0
  log_group_name     = "/pia/dev/app"
  region             = var.aws_region

  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE", value = "dev" },
    { name = "AWS_REGION", value = var.aws_region },
    { name = "SQS_QUEUE_URL", value = module.sqs.queue_url },
  ]
}

# ── CloudFront + S3 dashboard ─────────────────────────────────────────────────

module "cloudfront" {
  source = "../../modules/cloudfront"

  environment                      = "dev"
  app_name                         = "pia"
  dashboard_bucket_id              = module.s3.dashboard_bucket_id
  dashboard_bucket_regional_domain = module.s3.dashboard_bucket_regional_domain
}
