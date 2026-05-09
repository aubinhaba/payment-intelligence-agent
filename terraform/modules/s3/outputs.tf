output "reports_bucket_name" {
  description = "Reports S3 bucket name"
  value       = aws_s3_bucket.reports.bucket
}

output "reports_bucket_arn" {
  description = "Reports S3 bucket ARN (used in IAM policies)"
  value       = aws_s3_bucket.reports.arn
}

output "dashboard_bucket_name" {
  description = "Dashboard S3 bucket name"
  value       = aws_s3_bucket.dashboard.bucket
}

output "dashboard_bucket_arn" {
  description = "Dashboard S3 bucket ARN"
  value       = aws_s3_bucket.dashboard.arn
}

output "dashboard_bucket_regional_domain" {
  description = "Dashboard bucket regional domain (used by CloudFront)"
  value       = aws_s3_bucket.dashboard.bucket_regional_domain_name
}

output "dashboard_bucket_id" {
  description = "Dashboard bucket ID"
  value       = aws_s3_bucket.dashboard.id
}
