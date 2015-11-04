package pl.kask;

import pl.kask.dto.GroceryItemDto;
import pl.kask.model.GroceryItem;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/grocery")
public class GroceryRestService {

    private static GroceryService groceryService = new GroceryService();

    @GET
    @Path("/save/{name}")
    @Produces("application/json")
    public Response test(@PathParam("name") String name) {
        groceryService.persist(new GroceryItem("usy@owner.com", name, 1));
        return Response.accepted().build();
    }

    @GET
    @Path("/items/{name}")
    @Produces("application/json")
    public List<GroceryItemDto> getItems(@PathParam("name") String name) {
        List<GroceryItem> items = groceryService.findByOwner(name);
        List<GroceryItemDto> result = new ArrayList<>();
        for (GroceryItem item : items) {
            result.add(new GroceryItemDto(item));
        }
        return result;
    }
}
