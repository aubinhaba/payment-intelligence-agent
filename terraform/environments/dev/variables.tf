variable "alert_email" {
  description = "Email address for SNS alarm notifications (leave empty to skip subscription)"
  type        = string
  default     = ""
}
