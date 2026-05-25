variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "app_name" {
  description = "Application name used as resource prefix"
  type        = string
}

variable "dashboard_bucket_id" {
  description = "S3 bucket ID for the Angular dashboard"
  type        = string
}

variable "dashboard_bucket_regional_domain" {
  description = "S3 bucket regional domain name (for OAC origin)"
  type        = string
}

variable "price_class" {
  description = "CloudFront price class (PriceClass_100 = US/EU only, cheapest)"
  type        = string
  default     = "PriceClass_100"
}

variable "alb_dns_name" {
  description = "ALB DNS name used as the API backend origin (HTTP)"
  type        = string
}
