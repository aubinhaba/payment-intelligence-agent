variable "environment" {
  description = "Deployment environment (dev, prod)"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
  default     = "pia"
}

variable "metrics_namespace" {
  description = "CloudWatch custom metrics namespace"
  type        = string
  default     = "PIA"
}

variable "log_retention_days" {
  description = "CloudWatch log group retention in days"
  type        = number
  default     = 14

  validation {
    condition     = contains([1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653], var.log_retention_days)
    error_message = "log_retention_days must be a valid CloudWatch retention value."
  }
}

variable "kms_key_id" {
  description = "KMS key ARN for encrypting CloudWatch logs (required for PCI-DSS compliance)"
  type        = string
  default     = null
}

variable "alert_email" {
  description = "Email address for SNS alarm notifications (leave empty to skip subscription)"
  type        = string
  default     = ""
}

variable "dlq_name" {
  description = "SQS DLQ queue name — used to create the DLQ depth alarm"
  type        = string
}

variable "additional_dlq_names" {
  description = "Extra DLQ queue names to monitor (e.g. anomaly-analysis-dlq). One alarm per entry, same threshold as main DLQ."
  type        = list(string)
  default     = []
}

variable "claude_error_rate_threshold" {
  description = "Percentage of Claude API errors above which the alarm fires"
  type        = number
  default     = 5
}

variable "anomaly_min_detection_rate" {
  description = "Minimum anomaly detections per 5 min window (below = stuck consumer alarm). Set to 0 to disable alarm."
  type        = number
  default     = 5

  validation {
    condition     = var.anomaly_min_detection_rate >= 0
    error_message = "anomaly_min_detection_rate must be >= 0."
  }
}
