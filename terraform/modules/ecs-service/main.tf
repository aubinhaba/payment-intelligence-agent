locals {
  prefix    = "${var.app_name}-${var.environment}"
  full_name = "${local.prefix}-${var.service_name}"
}

resource "aws_ecs_task_definition" "service" {
  family                   = local.full_name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = var.execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([
    {
      name      = var.service_name
      image     = var.ecr_image_url
      essential = true

      portMappings = var.container_port > 0 ? [
        {
          containerPort = var.container_port
          protocol      = "tcp"
        }
      ] : []

      environment = var.environment_variables
      secrets     = var.secrets

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = var.log_group_name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = var.service_name
        }
      }
      # No container-level healthCheck: distroless image has no shell/curl.
      # pia-core health is monitored by the ALB target group (/actuator/health).
    }
  ])

  tags = {
    Name        = local.full_name
    Environment = var.environment
    Application = var.app_name
  }
}

resource "aws_ecs_service" "service" {
  name                   = local.full_name
  cluster                = var.cluster_id
  task_definition        = aws_ecs_task_definition.service.arn
  desired_count          = var.desired_count
  enable_execute_command = var.enable_execute_command

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 70
    base              = 0
  }

  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 30
    base              = 1
  }

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.security_group_id]
    assign_public_ip = false
  }

  dynamic "load_balancer" {
    for_each = var.target_group_arn != null ? [1] : []
    content {
      target_group_arn = var.target_group_arn
      container_name   = var.service_name
      container_port   = var.container_port
    }
  }

  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 200

  lifecycle {
    # Prevent Terraform from resetting task definition on every apply
    # (CI/CD manages image tag updates via aws ecs update-service)
    ignore_changes = [task_definition]
  }

  tags = {
    Name        = local.full_name
    Environment = var.environment
    Application = var.app_name
  }
}

# ── Auto-scaling ──────────────────────────────────────────────────────────────

resource "aws_appautoscaling_target" "service" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${split("/", var.cluster_id)[1]}/${aws_ecs_service.service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  name               = "${local.full_name}-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.service.resource_id
  scalable_dimension = aws_appautoscaling_target.service.scalable_dimension
  service_namespace  = aws_appautoscaling_target.service.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = var.cpu_scale_target
    scale_in_cooldown  = 300
    scale_out_cooldown = 60

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}
