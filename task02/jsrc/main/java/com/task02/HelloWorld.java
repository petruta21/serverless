package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		String path = requestEvent.getRequestContext().getHttp().getPath();
		String method = requestEvent.getRequestContext().getHttp().getMethod();

		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		Map<String, Object> resultMap = new HashMap<>();
		System.out.println("The path is " + path);
		if ("/hello".equalsIgnoreCase(path)) {
			response.setStatusCode(200);
			resultMap.put("statusCode", 200);
			resultMap.put("message", "Hello from Lambda");
		} else {
			response.setStatusCode(400);
			resultMap.put("statusCode", 400);
			resultMap.put("message", String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, method));
		}
		response.setHeaders(responseHeaders);
		response.setBody(gson.toJson(resultMap));
		return response;
	}
}
