variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "visibility_timeout_seconds" {
  description = "SQS visibility timeout — must be >= Spring listener ack timeout"
  type        = number
  default     = 300
}

variable "message_retention_seconds" {
  description = "How long SQS retains unprocessed messages"
  type        = number
  default     = 86400 # 1 day
}

variable "max_receive_count" {
  description = "Number of receive attempts before message goes to DLQ"
  type        = number
  default     = 3
}

variable "kms_key_id" {
  description = "KMS key ID for SQS server-side encryption (optional)"
  type        = string
  default     = null
}
