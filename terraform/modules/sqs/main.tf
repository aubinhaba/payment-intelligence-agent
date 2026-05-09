locals {
  prefix = "${var.app_name}-${var.environment}"
}

resource "aws_sqs_queue" "dlq" {
  name                      = "${local.prefix}-payment-events-dlq"
  message_retention_seconds = 1209600 # 14 days max
  kms_master_key_id         = var.kms_key_id

  tags = {
    Name        = "${local.prefix}-payment-events-dlq"
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_sqs_queue" "main" {
  name                       = "${local.prefix}-payment-events"
  visibility_timeout_seconds = var.visibility_timeout_seconds
  message_retention_seconds  = var.message_retention_seconds
  kms_master_key_id          = var.kms_key_id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })

  tags = {
    Name        = "${local.prefix}-payment-events"
    Environment = var.environment
    Application = var.app_name
  }
}
