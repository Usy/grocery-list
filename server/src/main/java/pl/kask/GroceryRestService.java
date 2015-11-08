package pl.kask;

import pl.kask.dto.GroceryItemDto;
import pl.kask.model.GroceryItem;

import javax.ws.rs.*;
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

    @POST
    @Path("/items")
    @Consumes("application/json")
    public void addItem(GroceryItemDto groceryItemDto) {
        GroceryItem newItem = new GroceryItem(
                groceryItemDto.getOwner(),
                groceryItemDto.getItemName(),
                groceryItemDto.getAmount()
        );
        groceryService.persist(newItem);
    }

    @DELETE
    @Path("/items/{name}/{item}")
    public void deleteItem(@PathParam("name") String name, @PathParam("item") String itemName) {
        List<GroceryItem> items = groceryService.findByOwner(name);
        for (GroceryItem item : items) {
            if (item.getItemName().equals(itemName)) {
                groceryService.delete(item);
                break;
            }
        }
    }

    @PUT
    @Path("/items")
    @Consumes("application/json")
    public void updateItem(GroceryItemDto groceryItemDto) {
        List<GroceryItem> items = groceryService.findByOwner(groceryItemDto.getOwner());
        GroceryItem oldItem = null;
        for (GroceryItem item : items) {
            if (item.getItemName().equals(groceryItemDto.getItemName())) {
                oldItem = item;
                break;
            }
        }
        if (oldItem == null) {
            return;
        }
        oldItem.setAmount(groceryItemDto.getAmount());
        groceryService.update(oldItem);
    }
}
