locals {
  prefix = "${var.app_name}-${var.environment}"
}

# ── Log Groups ────────────────────────────────────────────────────────────────

resource "aws_cloudwatch_log_group" "app" {
  name              = "/pia/${var.environment}/app"
  retention_in_days = var.log_retention_days
  kms_key_id        = var.kms_key_id

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_cloudwatch_log_group" "sqs" {
  name              = "/pia/${var.environment}/sqs"
  retention_in_days = var.log_retention_days
  kms_key_id        = var.kms_key_id

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

# ── Log Metric Filters ────────────────────────────────────────────────────────

resource "aws_cloudwatch_log_metric_filter" "app_errors" {
  name           = "${local.prefix}-app-errors"
  log_group_name = aws_cloudwatch_log_group.app.name
  pattern        = "[time, request_id, level=ERROR*, ...]"

  metric_transformation {
    name      = "pia.app.errors"
    namespace = var.metrics_namespace
    value     = "1"
    unit      = "Count"
  }
}

resource "aws_cloudwatch_log_metric_filter" "pan_exposure" {
  name           = "${local.prefix}-pan-exposure"
  log_group_name = aws_cloudwatch_log_group.app.name
  pattern        = "[time, request_id, level, logger, message=*PAN* | message=*card_number*]"

  metric_transformation {
    name      = "pia.security.pan.exposure"
    namespace = var.metrics_namespace
    value     = "1"
    unit      = "Count"
  }
}

# ── SNS Topic ─────────────────────────────────────────────────────────────────

resource "aws_sns_topic" "alerts" {
  name = "${local.prefix}-alerts"

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_sns_topic_subscription" "email" {
  count     = var.alert_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}

# ── CloudWatch Dashboard ──────────────────────────────────────────────────────

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${local.prefix}-overview"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Transactions Ingested"
          view   = "timeSeries"
          stat   = "Sum"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.transactions.ingested", { label = "Ingested" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Anomalies Detected"
          view   = "timeSeries"
          stat   = "Sum"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.anomalies.detected", { label = "Anomalies" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Claude Agent Latency (ms)"
          view   = "timeSeries"
          stat   = "p99"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.agent.claude.latency.millis", { label = "p99 latency" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "Claude API Errors"
          view   = "timeSeries"
          stat   = "Sum"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.agent.claude.errors", { label = "Errors" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "Claude Tokens Used"
          view   = "timeSeries"
          stat   = "Sum"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.agent.claude.tokens.used", "type", "input", { label = "Input tokens" }],
            [var.metrics_namespace, "pia.agent.claude.tokens.used", "type", "output", { label = "Output tokens" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "SQS Messages Consumed"
          view   = "timeSeries"
          stat   = "Sum"
          period = 300
          metrics = [
            [var.metrics_namespace, "pia.sqs.messages.consumed", { label = "Consumed" }]
          ]
        }
      }
    ]
  })
}

# ── Alarms ────────────────────────────────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "dlq_depth" {
  alarm_name          = "${local.prefix}-dlq-depth"
  alarm_description   = "Messages are stuck in the DLQ — check consumer errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = var.dlq_name
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_cloudwatch_metric_alarm" "claude_error_rate" {
  alarm_name          = "${local.prefix}-claude-error-rate"
  alarm_description   = "Claude API error rate exceeds ${var.claude_error_rate_threshold}%"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  threshold           = var.claude_error_rate_threshold
  treat_missing_data  = "notBreaching"

  metric_query {
    id          = "errors"
    return_data = false
    metric {
      metric_name = "pia.agent.claude.errors"
      namespace   = var.metrics_namespace
      period      = 300
      stat        = "Sum"
    }
  }

  metric_query {
    id          = "calls"
    return_data = false
    metric {
      metric_name = "pia.agent.claude.latency.millis"
      namespace   = var.metrics_namespace
      period      = 300
      stat        = "SampleCount"
    }
  }

  metric_query {
    id          = "error_rate"
    expression  = "IF(calls > 0, (errors / calls) * 100, 0)"
    label       = "Claude Error Rate %"
    return_data = true
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_cloudwatch_metric_alarm" "anomaly_detection_stuck" {
  count               = var.anomaly_min_detection_rate > 0 ? 1 : 0
  alarm_name          = "${local.prefix}-anomaly-detection-stuck"
  alarm_description   = "No anomaly detections in the last 5 minutes — consumer may be stuck"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = 2
  datapoints_to_alarm = 2
  metric_name         = "pia.anomalies.detected"
  namespace           = var.metrics_namespace
  period              = 300
  statistic           = "Sum"
  threshold           = var.anomaly_min_detection_rate
  treat_missing_data  = "notBreaching"

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_cloudwatch_metric_alarm" "app_error_rate" {
  alarm_name          = "${local.prefix}-app-error-rate"
  alarm_description   = "Application error rate exceeds threshold"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "pia.app.errors"
  namespace           = var.metrics_namespace
  period              = 300
  statistic           = "Sum"
  threshold           = 10
  treat_missing_data  = "notBreaching"

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_cloudwatch_metric_alarm" "pan_exposure_critical" {
  alarm_name          = "${local.prefix}-pan-exposure-CRITICAL"
  alarm_description   = "PAN detected in logs — IMMEDIATE PCI-DSS violation"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "pia.security.pan.exposure"
  namespace           = var.metrics_namespace
  period              = 60
  statistic           = "Sum"
  threshold           = 0
  treat_missing_data  = "notBreaching"

  alarm_actions = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
    Severity    = "CRITICAL"
  }
}

resource "aws_cloudwatch_metric_alarm" "ecs_task_unhealthy" {
  alarm_name          = "${local.prefix}-ecs-task-unhealthy"
  alarm_description   = "ECS running task count dropped below 1"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "RunningTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 60
  statistic           = "Average"
  threshold           = 1
  treat_missing_data  = "breaching"

  dimensions = {
    ClusterName = local.prefix
    ServiceName = local.prefix
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]

  tags = {
    Environment = var.environment
    Application = var.app_name
  }
}
