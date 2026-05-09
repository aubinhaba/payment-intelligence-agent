output "kms_key_id" {
  description = "ARN of the KMS key for CloudWatch logs encryption"
  value       = aws_kms_key.cloudwatch_logs.arn
}

output "kms_key_alias" {
  description = "Alias of the KMS key"
  value       = aws_kms_alias.cloudwatch_logs.name
}
