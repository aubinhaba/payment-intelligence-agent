output "table_name" {
  description = "DynamoDB table name"
  value       = aws_dynamodb_table.main.name
}

output "table_arn" {
  description = "DynamoDB table ARN (used in IAM policies)"
  value       = aws_dynamodb_table.main.arn
}
