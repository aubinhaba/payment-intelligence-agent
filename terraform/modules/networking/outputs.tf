output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (ALB, NAT)"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet IDs (ECS tasks)"
  value       = aws_subnet.private[*].id
}

output "sg_alb_id" {
  description = "Security group ID for the ALB"
  value       = aws_security_group.alb.id
}

output "sg_ecs_core_id" {
  description = "Security group ID for pia-core ECS tasks"
  value       = aws_security_group.ecs_core.id
}

output "sg_ecs_simulator_id" {
  description = "Security group ID for pia-simulator ECS tasks"
  value       = aws_security_group.ecs_simulator.id
}
