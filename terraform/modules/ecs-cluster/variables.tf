variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "fargate_spot_weight" {
  description = "Weight for Fargate Spot in the default capacity provider strategy"
  type        = number
  default     = 70
}

variable "fargate_weight" {
  description = "Weight for Fargate (on-demand) in the default capacity provider strategy"
  type        = number
  default     = 30
}
