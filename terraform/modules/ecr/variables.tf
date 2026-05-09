variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "repository_names" {
  description = "List of ECR repository short names (prefix will be added)"
  type        = list(string)
}

variable "image_count_to_keep" {
  description = "Number of images to retain per repository"
  type        = number
  default     = 10
}
