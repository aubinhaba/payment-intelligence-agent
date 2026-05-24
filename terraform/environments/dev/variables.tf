variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "eu-west-1"
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

variable "anomaly_analysis_visibility_timeout_seconds" {
  description = "Visibility timeout for the anomaly-analysis queue. Must be >= worst-case Claude tool-loop latency to avoid duplicate analyses."
  type        = number
  default     = 600
}

variable "anomaly_analysis_max_receive_count" {
  description = "Max receive attempts for the anomaly-analysis queue. Kept low because Claude failures are best-effort (transaction + anomalies already persisted)."
  type        = number
  default     = 2
}

variable "agent_enabled" {
  description = "Enable Claude agent analysis. Set to true only when testing the AI pipeline to avoid consuming API tokens."
  type        = bool
  default     = false
}

variable "simulator_desired_count" {
  description = "Number of pia-simulator ECS tasks to run. Set to 1 to generate continuous payment events, 0 to stop."
  type        = number
  default     = 0
}
