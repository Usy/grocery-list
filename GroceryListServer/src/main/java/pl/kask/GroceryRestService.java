package pl.kask;

import pl.kask.auth.VerificationService;
import pl.kask.dto.GroceryItemDto;
import pl.kask.dto.ShareRequest;
import pl.kask.dto.SynchronizationRequest;
import pl.kask.dto.SynchronizationResponse;
import pl.kask.model.AccountDao;
import pl.kask.model.GroceryItem;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/grocery")
public class GroceryRestService {

    private static final Logger log = Logger.getLogger(GroceryRestService.class.getName());

    private static AccountDao accountDao = new AccountDao();

    private static GroceryService groceryService = new GroceryService(accountDao);
    private static VerificationService verificationService = new VerificationService("922182942877-cp1133aa1h9ifade7fh5hdnk7bot1eus.apps.googleusercontent.com", accountDao);

    @POST
    @Path("/sync/{name}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response synchronize(@PathParam("name") String name, @HeaderParam("id_token") String idToken,
                                @HeaderParam("device_id") String deviceId, SynchronizationRequest request) {

        if (verificationService.isVerified(idToken, name)) {
            log.info(request.toString());

            SynchronizationResponse result = groceryService.synchronize(name, deviceId, request);
            log.info(result.toString());
            return Response.ok(result).build();
        } else {
            return Response.status(401).build();
        }
    }

    @POST
    @Path("/share/{name}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response share(@PathParam("name") String name, @HeaderParam("id_token") String idToken, ShareRequest request) {

        if (verificationService.isVerified(idToken, name)) {
            log.info(request.toString());

            String itemName = request.getItemName();
            String coOwnerMail = request.getCoOwnerMail();
            boolean result = groceryService.share(name, itemName, coOwnerMail);
            if (result) {
                return Response.ok().build();
            } else {
                return Response.status(400).build();
            }
        } else {
            return Response.status(401).build();
        }
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
                    groceryItemDto.getItemName()
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
                groceryService.update(oldItem);
            }
            return Response.ok().build();
        } else {
            return Response.status(401).build();
        }

    }
}
