locals {
  prefix = "${var.app_name}-${var.environment}"
}

# Single-table design: PK/SK + 4 GSIs covering all access patterns
# GSI1: card transaction history   (CARD#hash / TX#timestamp)
# GSI2: anomaly by severity        (ANOMALY#severity / timestamp)
# GSI3: daily reports              (REPORT#daily / date)
# GSI4: outbox unprocessed events  (OUTBOX#unprocessed / timestamp)
resource "aws_dynamodb_table" "main" {
  name         = "${local.prefix}-events"
  billing_mode = var.billing_mode
  hash_key     = "PK"
  range_key    = "SK"

  attribute {
    name = "PK"
    type = "S"
  }
  attribute {
    name = "SK"
    type = "S"
  }
  attribute {
    name = "GSI1PK"
    type = "S"
  }
  attribute {
    name = "GSI1SK"
    type = "S"
  }
  attribute {
    name = "GSI2PK"
    type = "S"
  }
  attribute {
    name = "GSI2SK"
    type = "S"
  }
  attribute {
    name = "GSI3PK"
    type = "S"
  }
  attribute {
    name = "GSI3SK"
    type = "S"
  }
  attribute {
    name = "GSI4PK"
    type = "S"
  }
  attribute {
    name = "GSI4SK"
    type = "S"
  }

  global_secondary_index {
    name            = "GSI1-card-transactions"
    hash_key        = "GSI1PK"
    range_key       = "GSI1SK"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "GSI2-anomaly-severity"
    hash_key        = "GSI2PK"
    range_key       = "GSI2SK"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "GSI3-reports-daily"
    hash_key        = "GSI3PK"
    range_key       = "GSI3SK"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "GSI4-outbox-unprocessed"
    hash_key        = "GSI4PK"
    range_key       = "GSI4SK"
    projection_type = "ALL"
  }

  point_in_time_recovery {
    enabled = var.point_in_time_recovery_enabled
  }

  # AWS-managed encryption key (no extra cost, suitable for dev)
  server_side_encryption {
    enabled = true
  }

  tags = {
    Name        = "${local.prefix}-events"
    Environment = var.environment
    Application = var.app_name
  }
}
