output "app_log_group_name" {
  description = "Name of the application CloudWatch log group"
  value       = aws_cloudwatch_log_group.app.name
}

output "app_log_group_arn" {
  description = "ARN of the application CloudWatch log group"
  value       = aws_cloudwatch_log_group.app.arn
}

output "sqs_log_group_name" {
  description = "Name of the SQS CloudWatch log group"
  value       = aws_cloudwatch_log_group.sqs.name
}

output "sqs_log_group_arn" {
  description = "ARN of the SQS CloudWatch log group"
  value       = aws_cloudwatch_log_group.sqs.arn
}

output "alerts_topic_arn" {
  description = "ARN of the SNS alerts topic"
  value       = aws_sns_topic.alerts.arn
}

output "dashboard_name" {
  description = "Name of the CloudWatch dashboard"
  value       = aws_cloudwatch_dashboard.main.dashboard_name
}

output "alarm_arns" {
  description = "Map of alarm names to ARNs"
  value = merge(
    {
      dlq_depth          = aws_cloudwatch_metric_alarm.dlq_depth.arn
      claude_error_rate  = aws_cloudwatch_metric_alarm.claude_error_rate.arn
      ecs_task_unhealthy = aws_cloudwatch_metric_alarm.ecs_task_unhealthy.arn
      app_error_rate     = aws_cloudwatch_metric_alarm.app_error_rate.arn
      pan_exposure       = aws_cloudwatch_metric_alarm.pan_exposure_critical.arn
    },
    var.anomaly_min_detection_rate > 0 ? {
      anomaly_detection_stuck = aws_cloudwatch_metric_alarm.anomaly_detection_stuck[0].arn
    } : {}
  )
}
