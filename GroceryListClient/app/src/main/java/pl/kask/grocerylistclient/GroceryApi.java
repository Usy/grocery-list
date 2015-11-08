package pl.kask.grocerylistclient;

import java.util.List;

import pl.kask.grocerylistclient.dto.GroceryItemDto;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface GroceryApi {

    @GET("/items/{user}")
    List<GroceryItemDto> fetchItems(@Path("user") String user);

    @POST("/items")
    void addItem(@Body GroceryItemDto groceryItemDto, Callback<Response> callback);

    @DELETE("/items/{user}/{item}")
    void deleteItem(@Path("user") String user, @Path("item") String item, Callback<Response> callback);

    @PUT("/items")
    void updateItem(@Body GroceryItemDto groceryItemDto, Callback<Response> callback);
}
