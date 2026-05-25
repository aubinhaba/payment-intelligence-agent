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
    name = "gsi1Pk"
    type = "S"
  }
  attribute {
    name = "gsi1Sk"
    type = "S"
  }
  attribute {
    name = "gsi2Pk"
    type = "S"
  }
  attribute {
    name = "gsi2Sk"
    type = "S"
  }
  attribute {
    name = "gsi3Pk"
    type = "S"
  }
  attribute {
    name = "gsi3Sk"
    type = "S"
  }
  attribute {
    name = "gsi4Pk"
    type = "S"
  }
  attribute {
    name = "gsi4Sk"
    type = "S"
  }

  global_secondary_index {
    name            = "gsi1"
    hash_key        = "gsi1Pk"
    range_key       = "gsi1Sk"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "gsi2"
    hash_key        = "gsi2Pk"
    range_key       = "gsi2Sk"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "gsi3"
    hash_key        = "gsi3Pk"
    range_key       = "gsi3Sk"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "gsi4"
    hash_key        = "gsi4Pk"
    range_key       = "gsi4Sk"
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
