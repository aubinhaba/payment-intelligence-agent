locals {
  prefix = "${var.app_name}-${var.environment}"
}

data "aws_iam_policy_document" "ecs_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# ── ECS Task Role — what the application code assumes ─────────────────────────

resource "aws_iam_role" "task" {
  name               = "${local.prefix}-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json

  tags = {
    Name        = "${local.prefix}-task-role"
    Environment = var.environment
    Application = var.app_name
  }
}

data "aws_iam_policy_document" "task" {
  # DynamoDB — least-privilege on the single table
  statement {
    sid    = "DynamoDBAccess"
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
    ]
    resources = [
      var.dynamodb_table_arn,
      "${var.dynamodb_table_arn}/index/*",
    ]
  }

  # SQS — receive from main queue, send to main queue (simulator path), DLQ read
  statement {
    sid    = "SQSAccess"
    effect = "Allow"
    actions = [
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility",
    ]
    resources = concat([var.sqs_queue_arn, var.sqs_dlq_arn], var.extra_sqs_queue_arns)
  }

  # S3 — read/write reports bucket
  statement {
    sid    = "S3ReportsAccess"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
    ]
    resources = ["${var.s3_reports_bucket_arn}/*"]
  }

  statement {
    sid       = "S3ReportsList"
    effect    = "Allow"
    actions   = ["s3:ListBucket"]
    resources = [var.s3_reports_bucket_arn]
  }

  # SSM — read application parameters
  statement {
    sid    = "SSMReadParameters"
    effect = "Allow"
    actions = [
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParametersByPath",
    ]
    resources = ["arn:aws:ssm:*:*:parameter${var.ssm_path_prefix}/*"]
  }

  # CloudWatch — publish business metrics
  statement {
    sid       = "CloudWatchMetrics"
    effect    = "Allow"
    actions   = ["cloudwatch:PutMetricData"]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "cloudwatch:namespace"
      values   = [var.cloudwatch_namespace]
    }
  }

  # CloudWatch Logs — structured log output
  statement {
    sid    = "CloudWatchLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
    resources = ["arn:aws:logs:*:*:log-group:/pia/${var.environment}/*:*"]
  }

  # X-Ray — OTEL traces
  statement {
    sid    = "XRayWrite"
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
    ]
    resources = ["*"]
  }

  # KMS — decrypt SQS messages and SSM SecureString parameters at runtime
  statement {
    sid    = "KMSDecryptRuntime"
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
      "kms:DescribeKey",
    ]
    resources = [var.kms_key_arn]
  }

  # ECS Exec — enable interactive debugging in dev
  statement {
    sid    = "ECSExec"
    effect = "Allow"
    actions = [
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "task" {
  name        = "${local.prefix}-task-policy"
  description = "Least-privilege policy for ${local.prefix} ECS tasks"
  policy      = data.aws_iam_policy_document.task.json
}

resource "aws_iam_role_policy_attachment" "task" {
  role       = aws_iam_role.task.name
  policy_arn = aws_iam_policy.task.arn
}

# ── ECS Execution Role — what the ECS agent uses to start tasks ───────────────

resource "aws_iam_role" "execution" {
  name               = "${local.prefix}-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json

  tags = {
    Name        = "${local.prefix}-execution-role"
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_iam_role_policy_attachment" "execution_managed" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "execution" {
  # Pull from ECR
  statement {
    sid    = "ECRAccess"
    effect = "Allow"
    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
    ]
    resources = ["*"]
  }

  # Inject SSM SecureString secrets into container environment
  statement {
    sid    = "SSMSecretsInjection"
    effect = "Allow"
    actions = [
      "ssm:GetParameters",
      "ssm:GetParameter",
    ]
    resources = ["arn:aws:ssm:*:*:parameter${var.ssm_path_prefix}/*"]
  }

  # Decrypt SSM SecureString values
  statement {
    sid       = "KMSDecrypt"
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "execution" {
  name        = "${local.prefix}-execution-policy"
  description = "Supplemental execution policy for ${local.prefix} ECS tasks"
  policy      = data.aws_iam_policy_document.execution.json
}

resource "aws_iam_role_policy_attachment" "execution_custom" {
  role       = aws_iam_role.execution.name
  policy_arn = aws_iam_policy.execution.arn
}
