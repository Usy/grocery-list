package pl.kask;

import pl.kask.auth.VerificationService;
import pl.kask.dto.GroceryItemDto;
import pl.kask.dto.SynchronizationRequest;
import pl.kask.dto.SynchronizationResponse;
import pl.kask.model.GroceryItem;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/grocery")
public class GroceryRestService {

    private static final Logger log = Logger.getLogger(GroceryRestService.class.getName());

    private static GroceryService groceryService = new GroceryService();
    private static VerificationService verificationService = new VerificationService("922182942877-cp1133aa1h9ifade7fh5hdnk7bot1eus.apps.googleusercontent.com");

    @POST
    @Path("/sync/{name}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response synchronize(@PathParam("name") String name, @HeaderParam("id_token") String idToken,
                                @HeaderParam("device_id") String deviceId, SynchronizationRequest request) {

        log.info(request.toString());

        List<GroceryItem> items = groceryService.findByOwner(name);

        List<String> productsToRemove = request.getProductsToRemove();
        List<GroceryItem> itemsToRemove = new ArrayList<>();
        items.stream().filter(item -> productsToRemove.contains(item.getItemName())).forEach(item -> {
            itemsToRemove.add(item);
            groceryService.delete(item);
        });
        items.removeAll(itemsToRemove);

        for (String productName : request.getProductsToAdd()) {
            if (items.stream().anyMatch(item -> item.getItemName().equals(productName))) {
                break;
            }

            GroceryItem newItem = new GroceryItem(name, productName, 0);
            groceryService.persist(newItem);
            items.add(newItem);
        }

        SynchronizationResponse result = new SynchronizationResponse();

        for (GroceryItem item : items) {
            if (!request.getSubSums().containsKey(item.getItemName())) {
                result.getProductsToAdd().add(item.getItemName());
            } else {
                int newSubSum = request.getSubSums().get(item.getItemName());
                item.getSubSums().put(deviceId, newSubSum);
                groceryService.update(item);
            }
            Integer totalAmount = item.getSubSums().values().stream().reduce(0, Integer::sum);
            result.getTotalAmounts().put(item.getItemName(), totalAmount);
        }

        for (String productName : request.getSubSums().keySet()) {
            if (items.stream().noneMatch(item -> item.getItemName().equals(productName))) {
                result.getProductsToRemove().add(productName);
            }
        }

        return Response.ok(result).build();
    }

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
