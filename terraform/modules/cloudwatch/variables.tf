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

variable "claude_error_rate_threshold" {
  description = "Percentage of Claude API errors above which the alarm fires"
  type        = number
  default     = 5
}

variable "anomaly_min_detection_rate" {
  description = "Minimum anomaly detections per 5 min window (below = stuck consumer alarm)"
  type        = number
  default     = 0
}
