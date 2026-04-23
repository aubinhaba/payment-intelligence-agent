#!/usr/bin/env bash
# Initialise LocalStack resources required by PIA (DynamoDB, SQS, S3, SSM).
# Run once after `docker compose up`, or after each `make up`.
set -euo pipefail

ENDPOINT="http://localhost:4566"
REGION="eu-west-1"
AWS="aws --endpoint-url ${ENDPOINT} --region ${REGION}"

TABLE="pia-table"
QUEUE="payment-events-queue"
DLQ="payment-events-dlq"
BUCKET="pia-reports"
CLAUDE_API_KEY="${CLAUDE_API_KEY:-local-placeholder-key}"

echo "==> Creating DynamoDB table: ${TABLE}"
${AWS} dynamodb create-table \
    --table-name "${TABLE}" \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
        AttributeName=gsi1Pk,AttributeType=S \
        AttributeName=gsi1Sk,AttributeType=S \
        AttributeName=gsi2Pk,AttributeType=S \
        AttributeName=gsi2Sk,AttributeType=S \
        AttributeName=gsi3Pk,AttributeType=S \
        AttributeName=gsi3Sk,AttributeType=S \
        AttributeName=gsi4Pk,AttributeType=S \
        AttributeName=gsi4Sk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --global-secondary-indexes \
        '[
          {
            "IndexName":"gsi1",
            "KeySchema":[
              {"AttributeName":"gsi1Pk","KeyType":"HASH"},
              {"AttributeName":"gsi1Sk","KeyType":"RANGE"}
            ],
            "Projection":{"ProjectionType":"ALL"}
          },
          {
            "IndexName":"gsi2",
            "KeySchema":[
              {"AttributeName":"gsi2Pk","KeyType":"HASH"},
              {"AttributeName":"gsi2Sk","KeyType":"RANGE"}
            ],
            "Projection":{"ProjectionType":"ALL"}
          },
          {
            "IndexName":"gsi3",
            "KeySchema":[
              {"AttributeName":"gsi3Pk","KeyType":"HASH"},
              {"AttributeName":"gsi3Sk","KeyType":"RANGE"}
            ],
            "Projection":{"ProjectionType":"ALL"}
          },
          {
            "IndexName":"gsi4",
            "KeySchema":[
              {"AttributeName":"gsi4Pk","KeyType":"HASH"},
              {"AttributeName":"gsi4Sk","KeyType":"RANGE"}
            ],
            "Projection":{"ProjectionType":"ALL"}
          }
        ]' \
    > /dev/null

echo "==> Creating SQS DLQ: ${DLQ}"
DLQ_URL=$(${AWS} sqs create-queue \
    --queue-name "${DLQ}" \
    --query 'QueueUrl' --output text)

DLQ_ARN=$(${AWS} sqs get-queue-attributes \
    --queue-url "${DLQ_URL}" \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' --output text)

echo "==> Creating SQS queue: ${QUEUE} (DLQ ARN: ${DLQ_ARN})"
${AWS} sqs create-queue \
    --queue-name "${QUEUE}" \
    --attributes "RedrivePolicy={\"deadLetterTargetArn\":\"${DLQ_ARN}\",\"maxReceiveCount\":\"3\"}" \
    > /dev/null

echo "==> Creating S3 bucket: ${BUCKET}"
${AWS} s3api create-bucket \
    --bucket "${BUCKET}" \
    --create-bucket-configuration LocationConstraint="${REGION}" \
    > /dev/null

echo "==> Storing SSM parameter: /pia/claude/api-key"
${AWS} ssm put-parameter \
    --name "/pia/claude/api-key" \
    --value "${CLAUDE_API_KEY}" \
    --type SecureString \
    --overwrite \
    > /dev/null

echo "LocalStack bootstrap complete."
