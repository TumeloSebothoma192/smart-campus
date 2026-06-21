package com.campus.rest;

import com.campus.model.ServiceRequest;
import com.campus.model.User;
import com.campus.service.CampusRequestRepository;
import com.campus.service.CampusRequestService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/request")
public class CampusRequestResource {
    private final CampusRequestService repository = new CampusRequestRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get/{requestID}")
    public ServiceRequest getRequest(@PathParam("requestID") long requestID) {
        return repository.findRequest(requestID);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get/all")
    public RequestForm getAllRequests() {
        return new RequestForm(repository.findAll());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/store")
    public String storeRequest(User user, ServiceRequest request) {
        repository.storeRequest(user, request);
        return "Campus service request record is inserted.";
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/update")
    public String updateRequest(ServiceRequest request) {
        boolean updated = repository.updateRequest(request);
        if (updated) {
            return "Campus service request record is updated.";
        }
        return "Campus service request record does not exist and cannot be updated.";
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/delete/{requestID}")
    public String deleteRequest(@PathParam("requestID") long requestID) {
        boolean deleted = repository.deleteRequest(requestID);
        if (deleted) {
            return "Campus service request record is deleted.";
        }
        return "Campus service request record does not exist and cannot be deleted.";
    }
}
