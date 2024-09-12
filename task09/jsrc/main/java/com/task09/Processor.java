package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;

import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "processor",
        roleName = "processor-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
        tracingMode = TracingMode.Active
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "DYNAMODB_TABLE_NAME", value = "${target_table}")
})
@LambdaUrlConfig
public class Processor implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
    private final String tableName = System.getenv("DYNAMODB_TABLE_NAME");

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final ObjectMapper objectMapper = new ObjectMapper();


    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {

        String forecastString = OpenMeteoAPI.getWeatherForecast();
        if (forecastString != null) {
            try {
                WeatherEntity weatherEntity = parseForecast(forecastString);
                WeatherDynamoDBEntity dynamoDBEntity = convertToWeatherDynamoDBEntity(weatherEntity);
                putToDynamoDB(dynamoDBEntity);
                return createSuccessResponse("Weather forecast saved successfully!");
            } catch (Exception e) {
                return createErrorResponse(500, "Failed to save forecast to DynamoDB.");
            }
        } else {
            return createErrorResponse(400, "Invalid request.");
        }
    }

    private APIGatewayV2HTTPResponse createSuccessResponse(String message) {
        return createResponse(200, message);
    }

    private APIGatewayV2HTTPResponse createErrorResponse(int statusCode, String message) {
        return createResponse(statusCode, message);
    }

    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        response.setHeaders(responseHeaders);
        return response;
    }

    WeatherEntity parseForecast(String forecastJson) throws JsonProcessingException {
        return objectMapper.readValue(forecastJson, WeatherEntity.class);
    }

    private void putToDynamoDB(WeatherDynamoDBEntity dynamoDBEntity) throws JsonProcessingException {

        String forecastJson = convertForecastToJson(dynamoDBEntity.getForecast());

        Table table = dynamoDB.getTable(tableName);
        Item item = new Item()
                .withPrimaryKey("id", dynamoDBEntity.getId())
                .withJSON("forecast", forecastJson);

        PutItemOutcome outcome = table.putItem(item);

        System.out.println("Weather forecast saved with outcome: " + outcome);
    }

    private String convertForecastToJson(WeatherDynamoDBEntity.Forecast forecast) throws JsonProcessingException {
        return objectMapper.writeValueAsString(forecast);
    }

    private WeatherDynamoDBEntity convertToWeatherDynamoDBEntity(WeatherEntity weatherEntity) {
        String eventId = UUID.randomUUID().toString();
        WeatherDynamoDBEntity.Forecast.Hourly hourly = new WeatherDynamoDBEntity.Forecast.Hourly();

        hourly.setTime(weatherEntity.getHourly().getTime());
        hourly.setTemperature_2m(weatherEntity.getHourly().getTemperature_2m());

        WeatherDynamoDBEntity.Forecast.HourlyUnits hourlyUnits = new WeatherDynamoDBEntity.Forecast.HourlyUnits();

        hourlyUnits.setTime(weatherEntity.getHourly_units().getTime());
        hourlyUnits.setTemperature_2m(weatherEntity.getHourly_units().getTemperature_2m());

        WeatherDynamoDBEntity result = new WeatherDynamoDBEntity();

        WeatherDynamoDBEntity.Forecast forecast = getForecast(weatherEntity, hourly, hourlyUnits);

        result.setId(eventId);
        result.setForecast(forecast);

        return result;
    }

    private static WeatherDynamoDBEntity.Forecast getForecast(WeatherEntity weatherEntity, WeatherDynamoDBEntity.Forecast.Hourly hourly, WeatherDynamoDBEntity.Forecast.HourlyUnits hourlyUnits) {
        WeatherDynamoDBEntity.Forecast forecast = new WeatherDynamoDBEntity.Forecast();

        forecast.setElevation(weatherEntity.getElevation());
        forecast.setGenerationtime_ms(weatherEntity.getGenerationtime_ms());
        forecast.setLatitude(weatherEntity.getLatitude());
        forecast.setLongitude(weatherEntity.getLongitude());
        forecast.setTimezone(weatherEntity.getTimezone());
        forecast.setTimezone_abbreviation(weatherEntity.getTimezone_abbreviation());
        forecast.setUtc_offset_seconds(weatherEntity.getUtc_offset_seconds());
        forecast.setHourly(hourly);
        forecast.setHourly_units(hourlyUnits);
        return forecast;
    }

}























