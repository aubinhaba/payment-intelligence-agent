locals {
  prefix = "${var.app_name}-${var.environment}"
}

resource "aws_ecr_repository" "repos" {
  for_each = toset(var.repository_names)

  name                 = "${local.prefix}-${each.key}"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name        = "${local.prefix}-${each.key}"
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_ecr_lifecycle_policy" "repos" {
  for_each   = aws_ecr_repository.repos
  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last ${var.image_count_to_keep} images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = var.image_count_to_keep
        }
        action = { type = "expire" }
      }
    ]
  })
}
