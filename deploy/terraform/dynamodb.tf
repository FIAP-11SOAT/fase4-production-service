resource "aws_dynamodb_table" "production" {
  name         = "${local.project_name}-production"
  billing_mode = "PAY_PER_REQUEST"

  hash_key = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "${local.project_name}-production"
  }
}
