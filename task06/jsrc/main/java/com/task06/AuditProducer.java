package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "audit_table_name", value = "${target_table}")
})
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
    private final String auditTableName = System.getenv("audit_table_name");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            String eventName = record.getEventName(); // INSERT, MODIFY
            Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
            Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

            Table auditTable = dynamoDB.getTable(auditTableName);
            if ("INSERT".equals(eventName)) {
                handleInsertEvent(newImage, auditTable);
            } else if ("MODIFY".equals(eventName)) {
                handleModifyEvent(newImage, oldImage, auditTable);
            }
        }

        return null;
    }

    private void handleInsertEvent(Map<String, AttributeValue> newImage, Table auditTable) {
        String key = newImage.get("key").getS();
        int value = Integer.parseInt(newImage.get("value").getN());

        Item auditItem = new Item()
                .withPrimaryKey("id", UUID.randomUUID().toString())
                .withString("itemKey", key)
                .withString("modificationTime", Instant.now().toString())
                .withMap("newValue", Map.of(
                        "key", key,
                        "value", value
                ));

        auditTable.putItem(auditItem);
    }

    private void handleModifyEvent(Map<String, AttributeValue> newImage, Map<String, AttributeValue> oldImage, Table auditTable) {
        String key = newImage.get("key").getS();
        int newValue = Integer.parseInt(newImage.get("value").getN());
        int oldValue = Integer.parseInt(oldImage.get("value").getN());

        Item auditItem = new Item()
                .withPrimaryKey("id", UUID.randomUUID().toString())
                .withString("itemKey", key)
                .withString("modificationTime", Instant.now().toString())
                .withString("updatedAttribute", "value")
                .withInt("oldValue", oldValue)
                .withInt("newValue", newValue);

        auditTable.putItem(auditItem);
    }
}
