terraform {
  backend "s3" {
    bucket         = "pia-terraform-state-dev"
    key            = "dev/terraform.tfstate"
    region         = "eu-west-1"
    encrypt        = true
    dynamodb_table = "pia-terraform-locks"
  }
}
