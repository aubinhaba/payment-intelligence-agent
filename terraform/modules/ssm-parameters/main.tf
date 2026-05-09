locals {
  path_prefix = "/${var.app_name}/${var.environment}"
}

# Claude API key — value must be set manually after apply:
#   aws ssm put-parameter \
#     --name /pia/dev/CLAUDE_API_KEY \
#     --value "sk-ant-..." \
#     --type SecureString \
#     --overwrite
# The placeholder here prevents Terraform from failing on first apply.
# ignore_changes on value so manual updates are not reverted.
resource "aws_ssm_parameter" "claude_api_key" {
  name        = "${local.path_prefix}/CLAUDE_API_KEY"
  description = "Anthropic Claude API key for PIA agent"
  type        = "SecureString"
  value       = "PLACEHOLDER_SET_MANUALLY"
  key_id      = var.kms_key_id

  lifecycle {
    ignore_changes = [value]
  }

  tags = {
    Environment = var.environment
    Application = var.app_name
    Secret      = "true"
  }
}

resource "aws_ssm_parameter" "log_level" {
  name        = "${local.path_prefix}/LOG_LEVEL"
  description = "Application log level"
  type        = "String"
  value       = "INFO"

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_ssm_parameter" "anomaly_amount_threshold" {
  name        = "${local.path_prefix}/ANOMALY_AMOUNT_THRESHOLD"
  description = "Transaction amount threshold for anomaly detection (EUR)"
  type        = "String"
  value       = "5000"

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_ssm_parameter" "anomaly_velocity_window_minutes" {
  name        = "${local.path_prefix}/ANOMALY_VELOCITY_WINDOW_MINUTES"
  description = "Velocity detection sliding window in minutes"
  type        = "String"
  value       = "60"

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_ssm_parameter" "anomaly_velocity_max_tx" {
  name        = "${local.path_prefix}/ANOMALY_VELOCITY_MAX_TX"
  description = "Maximum transactions per card within the velocity window"
  type        = "String"
  value       = "10"

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}
