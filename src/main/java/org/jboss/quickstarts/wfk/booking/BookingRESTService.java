/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.booking;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.NoSuchEntityException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

/**
 * <p>This class exposes the functionality of {@link BookingService} over HTTP endpoints as a RESTful resource via
 * JAX-RS.</p>
 *
 * <p>Full path for accessing the Contact resource is rest/contacts .</p>
 *
 * <p>The resource accepts and produces JSON.</p>
 * 
 * @author Joshua Wilson
 * @see BookingService
 * @see javax.ws.rs.core.Response
 */
/*
 * The Path annotation defines this as a REST Web Service using JAX-RS.
 * 
 * By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they 
 * can be overridden by adding the Consumes or Produces annotations to the individual method.
 * 
 * It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow 
 * transaction demarcation when accessing the database." - Antonio Goncalves
 * 
 */
@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class BookingRESTService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private BookingService service;
    
    /**
     * <p>Search for and return all the Contacts.  They are sorted alphabetically by name.</p>
     * 
     * @return A Response containing a list of Contacts
     */
    @GET
    public Response retrieveAllContacts() {
        List<Booking> customers = service.findAllOrderedByName();
        return Response.ok(customers).build();
    }

    /**
     * <p>Search for and return a Contact identified by email address.<p/>
     *
     * <p>Path annotation includes very simple regex to differentiate between email addresses and Ids.
     * <strong>DO NOT</strong> attempt to use this regex to validate email addresses.</p>
     *
     *
     * @param email The string parameter value provided as a Contact's email
     * @return A Response containing a single Contact
     */
    @GET
    @Path("/{email:^.+@.+$}")
    public Response retrieveContactsByEmail(@PathParam("email") String email) {
        Booking customer;
        try {
        	customer = service.findByEmail(email);
        } catch (NoResultException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(customer).build();
    }
    
    /**
     * <p>Search for and return a Contact identified by id.</p>
     * 
     * @param id The long parameter value provided as a Contact's id
     * @return A Response containing a single Contact
     */
    @GET
    @Path("/{id:[0-9]+}")
    public Response retrieveContactById(@PathParam("id") long id) {
        Booking customer = service.findById(id);
        if (customer == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
                + customer.getBirthDate() + " " + customer.getId());
        
        return Response.ok(customer).build();
    }

    /**
     * <p>Creates a new contact from the values provided. Performs validation and will return a JAX-RS response with either 200 (ok)
     * or with a map of fields, and related errors.</p>
     * 
     * @param contact The Contact object, constructed automatically from JSON input, to be <i>created</i> via {@link BookingService#create(Booking)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    public Response createContact(Booking customer) {
        log.info("createCustomer started. Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
            + customer.getBirthDate() + " " + customer.getId());
        if (customer == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder = null;

        try {
            // Go add the new Contact.
            service.create(customer);

            // Create a "Resource Created" 201 Response and pass the contact back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(customer);
            
            log.info("createCustomer completed. Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
                + customer.getBirthDate() + " " + customer.getId());
        } catch (ConstraintViolationException ce) {
            log.info("ConstraintViolationException - " + ce.toString());
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            log.info("ValidationException - " + e.toString());
            // Handle the unique constrain violation
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("email", "That email is already used, please use a unique email");
            builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    /**
     * <p>Updates a contact with the ID provided in the Contact. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.</p>
     * 
     * @param contact The Contact object, constructed automatically from JSON input, to be <i>updated</i> via {@link BookingService#update(Booking)}
     * @param id The long parameter value provided as the id of the Contact to be updated
     * @return A Response indicating the outcome of the create operation
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response updateCustomer(@PathParam("id") long id, Booking customer) {
        if (customer == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        log.info("updateCustomer started. Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
                + customer.getBirthDate() + " " + customer.getId());

        if (customer.getId() != id) {
            // The client attempted to update the read-only Id. This is not permitted.
            Response response = Response.status(Response.Status.CONFLICT).entity("The customer ID cannot be modified").build();
            throw new WebApplicationException(response);
        }
        if (service.findById(customer.getId()) == null) {
            // Verify if the contact exists. Return 404, if not present.
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        Response.ResponseBuilder builder = null;
        
        try {
            // Apply the changes the Contact.
            service.update(customer);

            // Create an OK Response and pass the contact back in case it is needed.
            builder = Response.ok(customer);

            log.info("updateCustomer completed. Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
                + customer.getBirthDate() + " " + customer.getId());
        } catch (ConstraintViolationException ce) {
            log.info("ConstraintViolationException - " + ce.toString());
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            log.info("ValidationException - " + e.toString());
            // Handle the unique constrain violation
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("email", "That email is already used, please use a unique email");
            responseObj.put("error", "This is where errors are displayed that are not related to a specific field");
            responseObj.put("anotherError", "You can find this error message in /src/main/java/org/jboss/quickstarts/wfk/rest/ContactRESTService.java line 242.");
            builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    /**
     * <p>Deletes a contact using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 200 OK or with a map of fields, and related errors.</p>
     * 
     * @param id The Long parameter value provided as the id of the Contact to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteContact(@PathParam("id") Long id) {
        log.info("deleteCustomer started. Customer ID = " + id);
        Response.ResponseBuilder builder = null;

        try {
            Booking customer = service.findById(id);
            if (customer != null) {
                service.delete(customer);
            } else {
                log.info("CustomerRESTService - deleteCustomer - No customer with matching ID was found so can't Delete.");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            builder = Response.noContent();
            log.info("deleteCustomer completed. Customer = " + customer.getFirstName() + " " + customer.getLastName() + " " + customer.getEmail() + " " + customer.getPhoneNumber() + " "
                + customer.getBirthDate() + " " + customer.getId());
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }
    
    /**
     * <p>Creates a JAX-RS "Bad Request" response including a map of all violation fields, and their message. This can be used
     * by calling client applications to display violations to users.<p/>
     * 
     * @param violations A Set of violations that need to be reported in the Response body
     * @return A Bad Request (400) Response containing all violation messages
     */
    private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }


}
