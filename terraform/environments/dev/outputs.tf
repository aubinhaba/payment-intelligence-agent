output "aws_region" {
  description = "AWS region used for this deployment"
  value       = var.aws_region
}

output "alb_dns_name" {
  description = "ALB DNS name — use this as API base URL"
  value       = module.alb.alb_dns_name
}

output "cloudfront_domain" {
  description = "CloudFront domain for the Angular dashboard"
  value       = module.cloudfront.distribution_domain_name
}

output "ecr_core_url" {
  description = "ECR URL for pia-core (docker push target)"
  value       = module.ecr.repository_urls["pia-core"]
}

output "ecr_simulator_url" {
  description = "ECR URL for pia-simulator (docker push target)"
  value       = module.ecr.repository_urls["pia-simulator"]
}

output "sqs_queue_url" {
  description = "SQS main queue URL (payment events)"
  value       = module.sqs.queue_url
}

output "sqs_anomaly_analysis_queue_url" {
  description = "SQS anomaly-analysis queue URL (Claude pipeline)"
  value       = module.sqs_anomaly_analysis.queue_url
}

output "dynamodb_table_name" {
  description = "DynamoDB table name"
  value       = module.dynamodb.table_name
}

output "s3_reports_bucket" {
  description = "S3 reports bucket name"
  value       = module.s3.reports_bucket_name
}

output "ssm_path_prefix" {
  description = "SSM parameter path prefix — set CLAUDE_API_KEY here after apply"
  value       = module.ssm.path_prefix
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs_cluster.cluster_name
}

output "dashboard_bucket_name" {
  description = "S3 bucket name for the Angular dashboard static files"
  value       = module.s3.dashboard_bucket_name
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID — used to invalidate cache after dashboard deploy"
  value       = module.cloudfront.distribution_id
}
