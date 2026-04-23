package com.aubin.pia.infrastructure;

import java.util.List;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/** Shared test infrastructure: LocalStack container helpers and table factory. */
public final class LocalStackTestSupport {

    public static final String TABLE_NAME = "pia-test-table";
    public static final DockerImageName LOCALSTACK_IMAGE =
            DockerImageName.parse("localstack/localstack:3.8");

    private LocalStackTestSupport() {}

    public static LocalStackContainer newContainer(Service... services) {
        return new LocalStackContainer(LOCALSTACK_IMAGE).withServices(services);
    }

    public static DynamoDbClient dynamoDbClient(LocalStackContainer localStack) {
        return DynamoDbClient.builder()
                .endpointOverride(localStack.getEndpointOverride(Service.DYNAMODB))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")))
                .region(Region.EU_WEST_1)
                .build();
    }

    public static DynamoDbEnhancedClient enhancedClient(LocalStackContainer localStack) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient(localStack)).build();
    }

    public static void createPiaTable(DynamoDbClient client) {
        client.createTable(
                CreateTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .billingMode(BillingMode.PAY_PER_REQUEST)
                        .attributeDefinitions(buildAttributeDefinitions())
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName("pk")
                                        .keyType(KeyType.HASH)
                                        .build(),
                                KeySchemaElement.builder()
                                        .attributeName("sk")
                                        .keyType(KeyType.RANGE)
                                        .build())
                        .globalSecondaryIndexes(buildGsis())
                        .build());
    }

    private static List<AttributeDefinition> buildAttributeDefinitions() {
        return List.of(
                attr("pk"), attr("sk"),
                attr("gsi1Pk"), attr("gsi1Sk"),
                attr("gsi2Pk"), attr("gsi2Sk"),
                attr("gsi3Pk"), attr("gsi3Sk"),
                attr("gsi4Pk"), attr("gsi4Sk"));
    }

    private static AttributeDefinition attr(String name) {
        return AttributeDefinition.builder()
                .attributeName(name)
                .attributeType(ScalarAttributeType.S)
                .build();
    }

    private static List<GlobalSecondaryIndex> buildGsis() {
        return List.of(
                gsi("gsi1", "gsi1Pk", "gsi1Sk"),
                gsi("gsi2", "gsi2Pk", "gsi2Sk"),
                gsi("gsi3", "gsi3Pk", "gsi3Sk"),
                gsi("gsi4", "gsi4Pk", "gsi4Sk"));
    }

    private static GlobalSecondaryIndex gsi(String name, String pkAttr, String skAttr) {
        return GlobalSecondaryIndex.builder()
                .indexName(name)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(pkAttr)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(skAttr)
                                .keyType(KeyType.RANGE)
                                .build())
                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                .build();
    }
}
