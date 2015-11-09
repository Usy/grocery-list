package pl.kask;

import pl.kask.auth.VerificationService;
import pl.kask.dto.GroceryItemDto;
import pl.kask.model.GroceryItem;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/grocery")
public class GroceryRestService {

    private static GroceryService groceryService = new GroceryService();
    private static VerificationService verificationService = new VerificationService("922182942877-cp1133aa1h9ifade7fh5hdnk7bot1eus.apps.googleusercontent.com");

    @GET
    @Path("/save/{name}")
    @Produces("application/json")
    public Response test(@PathParam("name") String name, @HeaderParam("id_token") String idToken) {

        if (verificationService.isVerified(idToken, name)) {
            return Response.ok().build();
        } else {
            return Response.status(401).build();
        }
    }

    @GET
    @Path("/items/{name}")
    @Produces("application/json")
    public Response getItems(@PathParam("name") String name, @HeaderParam("id_token") String idToken) {
        if (verificationService.isVerified(idToken, name)) {
            List<GroceryItem> items = groceryService.findByOwner(name);
            List<GroceryItemDto> result = new ArrayList<>();
            for (GroceryItem item : items) {
                result.add(new GroceryItemDto(item));
            }
            return Response.ok(result).build();
        } else {
            return Response.status(401).build();
        }
    }

    @POST
    @Path("/items")
    @Consumes("application/json")
    public Response addItem(GroceryItemDto groceryItemDto, @HeaderParam("id_token") String idToken) {
        String name = groceryItemDto.getOwner();
        if (verificationService.isVerified(idToken, name)) {
            GroceryItem newItem = new GroceryItem(
                    name,
                    groceryItemDto.getItemName(),
                    groceryItemDto.getAmount()
            );
            groceryService.persist(newItem);
            return Response.ok().build();
        } else {
            return Response.status(401).build();
        }

    }

    @DELETE
    @Path("/items/{name}/{item}")
    public Response deleteItem(@PathParam("name") String name, @PathParam("item") String itemName, @HeaderParam("id_token") String idToken) {
        if (verificationService.isVerified(idToken, name)) {
            List<GroceryItem> items = groceryService.findByOwner(name);
            for (GroceryItem item : items) {
                if (item.getItemName().equals(itemName)) {
                    groceryService.delete(item);
                    break;
                }
            }
            return Response.ok().build();
        } else {
            return Response.status(401).build();
        }
    }

    @PUT
    @Path("/items")
    @Consumes("application/json")
    public Response updateItem(GroceryItemDto groceryItemDto, @HeaderParam("id_token") String idToken) {
        String name = groceryItemDto.getOwner();
        if (verificationService.isVerified(idToken, name)) {
            List<GroceryItem> items = groceryService.findByOwner(name);
            GroceryItem oldItem = null;
            for (GroceryItem item : items) {
                if (item.getItemName().equals(groceryItemDto.getItemName())) {
                    oldItem = item;
                    break;
                }
            }
            if (oldItem != null) {
                oldItem.setAmount(groceryItemDto.getAmount());
                groceryService.update(oldItem);
            }
            return Response.ok().build();
        } else {
            return Response.status(401).build();
        }

    }
}
