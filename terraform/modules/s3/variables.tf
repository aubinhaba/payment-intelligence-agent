variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "account_id" {
  description = "AWS account ID (used to build globally unique bucket names)"
  type        = string
}
