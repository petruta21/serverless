package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(
        lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})

public class UuidGenerator implements RequestHandler<ScheduledEvent, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    String bucketName = System.getenv("target_bucket");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        try {
            List<String> uuidList = generateUUIDs(10);

            // file content as json
            String fileContent = objectMapper.writeValueAsString(new UuidList(uuidList));

            // file name
            ZonedDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC);
            String fileName = utcTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));

            // Upload the file to S3
            uploadToS3(bucketName, fileName, fileContent);

            return "UUIDs stored successfully in " + fileName;
        } catch (Exception e) {
            e.printStackTrace(); // this is fine, in the context of lambda execution all stdout is being forwarded to CW logs
            return "Error " + e.getMessage();
        }
    }

    private List<String> generateUUIDs(int count) {
    return IntStream.range(0, count)
            .mapToObj(i->UUID.randomUUID().toString())
            .collect(Collectors.toList());
    }

    private void uploadToS3(String bucketName, String fileName, String content) {
        byte[] contentAsBytes = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentAsBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentAsBytes.length);
        metadata.setContentType("text/plain");

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
        s3Client.putObject(request);
    }
}
