variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "kms_key_id" {
  description = "KMS key ID for SecureString parameters"
  type        = string
}
