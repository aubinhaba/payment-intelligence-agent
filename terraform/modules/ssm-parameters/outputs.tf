output "path_prefix" {
  description = "SSM parameter path prefix (e.g. /pia/dev)"
  value       = local.path_prefix
}

output "claude_api_key_arn" {
  description = "ARN of the Claude API key SSM parameter"
  value       = aws_ssm_parameter.claude_api_key.arn
}

output "parameter_arns" {
  description = "ARNs of all SSM parameters (used in IAM policy)"
  value = [
    aws_ssm_parameter.claude_api_key.arn,
    aws_ssm_parameter.log_level.arn,
    aws_ssm_parameter.anomaly_amount_threshold.arn,
    aws_ssm_parameter.anomaly_velocity_window_minutes.arn,
    aws_ssm_parameter.anomaly_velocity_max_tx.arn,
  ]
}
