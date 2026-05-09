module "cloudwatch" {
  source = "../../modules/cloudwatch"

  environment        = "dev"
  app_name           = "pia"
  metrics_namespace  = "PIA"
  log_retention_days = 7

  # Set via TF_VAR_alert_email or leave empty to skip subscription
  alert_email = var.alert_email

  # DLQ name must match the SQS module output
  dlq_name = module.sqs.dlq_name

  claude_error_rate_threshold = 10
  anomaly_min_detection_rate  = 0
}
