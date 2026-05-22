locals {
  prefix     = "${var.app_name}-${var.environment}"
  queue_name = "${local.prefix}-${var.queue_basename}"
  dlq_name   = "${local.prefix}-${var.queue_basename}-dlq"
}

resource "aws_sqs_queue" "dlq" {
  name                      = local.dlq_name
  message_retention_seconds = 1209600 # 14 days max
  kms_master_key_id         = var.kms_key_id

  tags = {
    Name        = local.dlq_name
    Environment = var.environment
    Application = var.app_name
    QueueRole   = "dlq"
    QueueGroup  = var.queue_basename
  }
}

resource "aws_sqs_queue" "main" {
  name                       = local.queue_name
  visibility_timeout_seconds = var.visibility_timeout_seconds
  message_retention_seconds  = var.message_retention_seconds
  kms_master_key_id          = var.kms_key_id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })

  tags = {
    Name        = local.queue_name
    Environment = var.environment
    Application = var.app_name
    QueueRole   = "main"
    QueueGroup  = var.queue_basename
  }
}
