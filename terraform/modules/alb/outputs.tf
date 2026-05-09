output "alb_dns_name" {
  description = "ALB public DNS name"
  value       = aws_lb.main.dns_name
}

output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

output "core_target_group_arn" {
  description = "Target group ARN for pia-core (used by ECS service)"
  value       = aws_lb_target_group.core.arn
}
