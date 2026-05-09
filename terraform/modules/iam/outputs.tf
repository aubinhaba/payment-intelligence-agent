output "task_role_arn" {
  description = "ARN of the ECS task role (application identity)"
  value       = aws_iam_role.task.arn
}

output "execution_role_arn" {
  description = "ARN of the ECS execution role (ECS agent identity)"
  value       = aws_iam_role.execution.arn
}
