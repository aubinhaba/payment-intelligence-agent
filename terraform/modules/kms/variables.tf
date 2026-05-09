variable "environment" {
  description = "Deployment environment (dev, prod)"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
  default     = "pia"
}

variable "deletion_window_in_days" {
  description = "KMS key deletion window in days (7-30). Use 7 for dev/testing to speed up cleanup."
  type        = number
  default     = 7

  validation {
    condition     = var.deletion_window_in_days >= 7 && var.deletion_window_in_days <= 30
    error_message = "deletion_window_in_days must be between 7 and 30."
  }
}
