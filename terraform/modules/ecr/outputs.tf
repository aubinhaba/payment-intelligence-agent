output "repository_urls" {
  description = "Map of short name → ECR repository URL"
  value       = { for k, v in aws_ecr_repository.repos : k => v.repository_url }
}

output "repository_arns" {
  description = "List of all ECR repository ARNs (used in IAM policies)"
  value       = [for v in aws_ecr_repository.repos : v.arn]
}
