output "distribution_domain_name" {
  description = "CloudFront distribution domain (e.g. d1234.cloudfront.net)"
  value       = aws_cloudfront_distribution.dashboard.domain_name
}

output "distribution_id" {
  description = "CloudFront distribution ID (used for cache invalidation)"
  value       = aws_cloudfront_distribution.dashboard.id
}
