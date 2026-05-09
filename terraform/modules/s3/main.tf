locals {
  prefix = "${var.app_name}-${var.environment}"
  # Account ID in bucket name ensures global uniqueness without random suffixes
  reports_bucket_name   = "${local.prefix}-reports-${var.account_id}"
  dashboard_bucket_name = "${local.prefix}-dashboard-${var.account_id}"
}

# ── Reports bucket (private, versioned, tiered storage) ───────────────────────

resource "aws_s3_bucket" "reports" {
  bucket = local.reports_bucket_name

  tags = {
    Name        = local.reports_bucket_name
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_s3_bucket_versioning" "reports" {
  bucket = aws_s3_bucket.reports.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "reports" {
  bucket = aws_s3_bucket.reports.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "reports" {
  bucket = aws_s3_bucket.reports.id

  rule {
    id     = "tiered-storage"
    status = "Enabled"
    filter {}

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "reports" {
  bucket                  = aws_s3_bucket.reports.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ── Dashboard bucket (private, served via CloudFront OAC) ─────────────────────

resource "aws_s3_bucket" "dashboard" {
  bucket = local.dashboard_bucket_name

  tags = {
    Name        = local.dashboard_bucket_name
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "dashboard" {
  bucket = aws_s3_bucket.dashboard.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "dashboard" {
  bucket                  = aws_s3_bucket.dashboard.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
