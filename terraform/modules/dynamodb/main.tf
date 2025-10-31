# DynamoDB Table for Productions
resource "aws_dynamodb_table" "productions" {
  name           = "${var.project_name}-${var.environment}-productions"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "orderId"
    type = "N"
  }

  attribute {
    name = "status"
    type = "S"
  }

  attribute {
    name = "createdAt"
    type = "S"
  }

  # Global Secondary Index for querying by orderId
  global_secondary_index {
    name     = "OrderIdIndex"
    hash_key = "orderId"

    projection_type = "ALL"
  }

  # Global Secondary Index for querying by status
  global_secondary_index {
    name     = "StatusIndex"
    hash_key = "status"
    range_key = "createdAt"

    projection_type = "ALL"
  }

  # Enable point-in-time recovery
  point_in_time_recovery {
    enabled = true
  }

  # Server-side encryption
  server_side_encryption {
    enabled = true
  }

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-productions"
  })
}

# DynamoDB Table for Production Events (optional for audit trail)
resource "aws_dynamodb_table" "production_events" {
  name           = "${var.project_name}-${var.environment}-production-events"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "productionId"
  range_key      = "timestamp"

  attribute {
    name = "productionId"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "S"
  }

  attribute {
    name = "eventType"
    type = "S"
  }

  # Global Secondary Index for querying by event type
  global_secondary_index {
    name     = "EventTypeIndex"
    hash_key = "eventType"
    range_key = "timestamp"

    projection_type = "ALL"
  }

  # TTL for automatic cleanup of old events (30 days)
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  # Enable point-in-time recovery
  point_in_time_recovery {
    enabled = true
  }

  # Server-side encryption
  server_side_encryption {
    enabled = true
  }

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-events"
  })
}