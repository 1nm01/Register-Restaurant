package org.khana;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.khana.entity.Restaurant;
import org.khana.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

public class RegisterRestaurant implements RequestHandler<APIGatewayProxyRequestEvent, ApiResponse> {
    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    private static final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client);
    private static final DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder().withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT).build();

    private static final String itemName = "!";
    @Override
    public ApiResponse handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        Gson gson = new Gson();
        Restaurant menu = null;
        LambdaLogger logger = context.getLogger();
        try {
            menu = gson.fromJson(apiGatewayProxyRequestEvent.getBody(), Restaurant.class);
        } catch (JsonSyntaxException e) {
            logger.log(e.getMessage());
        }
        assert menu != null;
        if(menu.getRestName().isEmpty()){
            return failure();
        }
        menu.setItemName(itemName);
        dynamoDBMapper.save(menu, dynamoDBMapperConfig);
        Map<String,String> headers = Map.of("content-type", "application/json");
        return new ApiResponse(201, headers, menu.toString());
    }
    private ApiResponse failure(){
        Map<String,String> headers = new HashMap<>();
        return new ApiResponse(400, headers, "{\"message\" : \"Specify Restaurant Name\"}");
    }
}
