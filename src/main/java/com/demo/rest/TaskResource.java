package com.demo.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    TaskRepository repository;

    @GET
    public List<Task> list() {
        return repository.findAll();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return repository.findById(id)
                .map(task -> Response.ok(task).build())
                .orElseGet(() -> notFound(id));
    }

    @POST
    public Response create(Task task, @Context UriInfo uriInfo) {
        if (isInvalid(task)) {
            return badRequest();
        }
        Task created = repository.create(task);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Task task) {
        if (isInvalid(task)) {
            return badRequest();
        }
        return repository.update(id, task)
                .map(updated -> Response.ok(updated).build())
                .orElseGet(() -> notFound(id));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        return repository.delete(id)
                ? Response.noContent().build()
                : notFound(id);
    }

    private static boolean isInvalid(Task task) {
        return task == null || task.getTitle() == null || task.getTitle().isBlank();
    }

    private static Response badRequest() {
        return error(Response.Status.BAD_REQUEST, "title must not be blank");
    }

    private static Response notFound(Long id) {
        return error(Response.Status.NOT_FOUND, "task " + id + " not found");
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build();
    }
}
