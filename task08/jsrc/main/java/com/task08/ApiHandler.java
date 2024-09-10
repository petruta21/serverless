package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.task08.sdk.OpenMeteoAPI;

import java.util.Map;

@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role",
        layers = {"sdk-layer"},
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig
@LambdaLayer(
        layerName = "sdk-layer",
        libraries = {"lib/task08-1.0.0-sdk.jar"},
        runtime = DeploymentRuntime.JAVA11
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        String forecastString = OpenMeteoAPI.getWeatherForecast();
        String path = requestEvent.getRequestContext().getHttp().getPath();
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        if ("/weather".equalsIgnoreCase(path)) {
            response.setStatusCode(200);
            response.setBody(forecastString);
        } else {
            response.setStatusCode(400);
        }
        response.setHeaders(responseHeaders);
        return response;
    }
}
