variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table the application accesses"
  type        = string
}

variable "sqs_queue_arn" {
  description = "ARN of the main SQS queue"
  type        = string
}

variable "sqs_dlq_arn" {
  description = "ARN of the SQS dead-letter queue"
  type        = string
}

variable "s3_reports_bucket_arn" {
  description = "ARN of the S3 reports bucket"
  type        = string
}

variable "ecr_repository_arns" {
  description = "List of ECR repository ARNs (for execution role pull permissions)"
  type        = list(string)
}

variable "ssm_path_prefix" {
  description = "SSM parameter path prefix the task role may read (e.g. /pia/dev)"
  type        = string
}

variable "cloudwatch_namespace" {
  description = "CloudWatch metrics namespace the task role may publish to"
  type        = string
  default     = "PIA"
}
