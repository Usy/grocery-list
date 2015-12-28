package pl.kask.grocerylistclient;

import java.util.List;

import pl.kask.grocerylistclient.dto.GroceryItemDto;
import pl.kask.grocerylistclient.dto.ShareRequest;
import pl.kask.grocerylistclient.dto.SynchronizationRequest;
import pl.kask.grocerylistclient.dto.SynchronizationResponse;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface GroceryApi {

    @GET("/items/{user}")
    List<GroceryItemDto> fetchItems(@Path("user") String user, @Header("id_token") String idToken,
                    @Header("device_id") String deviceId);

    @POST("/items")
    void addItem(@Body GroceryItemDto groceryItemDto, @Header("id_token") String idToken,
                    @Header("device_id") String deviceId, Callback<Response> callback);

    @DELETE("/items/{user}/{item}")
    void deleteItem(@Path("user") String user, @Path("item") String item,
                    @Header("id_token") String idToken, @Header("device_id") String deviceId,
                    Callback<Response> callback);

    @PUT("/items")
    void updateItem(@Body GroceryItemDto groceryItemDto, @Header("id_token") String idToken,
                    @Header("device_id") String deviceId, Callback<Response> callback);

    @POST("/sync/{user}")
    SynchronizationResponse synchronize(@Path("user") String user, @Body SynchronizationRequest request,
                    @Header("id_token") String idToken, @Header("device_id") String deviceId);

    @POST("/share/{user}")
    void share(@Path("user") String user, @Header("id_token") String idToken,
               @Body ShareRequest request, Callback<Response> callback);
}
