variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "billing_mode" {
  description = "DynamoDB billing mode: PAY_PER_REQUEST or PROVISIONED"
  type        = string
  default     = "PAY_PER_REQUEST"
}

variable "point_in_time_recovery_enabled" {
  description = "Enable point-in-time recovery (PITR)"
  type        = bool
  default     = true
}
