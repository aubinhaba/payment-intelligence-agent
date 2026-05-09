terraform {
  backend "s3" {
    bucket         = "pia-terraform-state-dev"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "pia-terraform-locks"
  }
}
