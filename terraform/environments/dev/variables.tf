variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "alert_email" {
  description = "Email address for CloudWatch SNS alarm notifications (leave empty to skip)"
  type        = string
  default     = ""
}

variable "image_tag" {
  description = "Docker image tag to deploy (updated by CI after each build)"
  type        = string
  default     = "latest"
}
