variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "service_name" {
  description = "Short service name (e.g. pia-core, pia-simulator)"
  type        = string
}

variable "cluster_id" {
  description = "ECS cluster ID"
  type        = string
}

variable "task_cpu" {
  description = "Task-level CPU units (256, 512, 1024, 2048, 4096)"
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Task-level memory in MiB"
  type        = number
  default     = 1024
}

variable "container_port" {
  description = "Container port exposed (0 to disable port mapping)"
  type        = number
  default     = 8080
}

variable "ecr_image_url" {
  description = "Full ECR image URL including tag"
  type        = string
}

variable "execution_role_arn" {
  description = "ARN of the ECS execution role"
  type        = string
}

variable "task_role_arn" {
  description = "ARN of the ECS task role"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "security_group_id" {
  description = "Security group ID for the ECS tasks"
  type        = string
}

variable "target_group_arn" {
  description = "ALB target group ARN (null for services without an ALB)"
  type        = string
  default     = null
}

variable "desired_count" {
  description = "Desired number of running tasks (0 until images are pushed)"
  type        = number
  default     = 0
}

variable "enable_execute_command" {
  description = "Enable ECS Exec for interactive debugging in dev"
  type        = bool
  default     = true
}

variable "environment_variables" {
  description = "Plain-text environment variables injected into the container"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "secrets" {
  description = "SSM SecureString parameters injected as environment variables"
  type = list(object({
    name      = string
    valueFrom = string
  }))
  default = []
}

variable "log_group_name" {
  description = "CloudWatch log group name for container logs"
  type        = string
}

variable "region" {
  description = "AWS region"
  type        = string
}

variable "min_capacity" {
  description = "Minimum task count for auto-scaling"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum task count for auto-scaling"
  type        = number
  default     = 4
}

variable "cpu_scale_target" {
  description = "Target CPU utilization percentage for auto-scaling"
  type        = number
  default     = 60
}
