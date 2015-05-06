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
package org.jboss.quickstarts.wfk.taxi;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import java.net.URI;
import java.util.List;

import java.util.logging.Logger;

/**
 * <p>This Service assumes the Control responsibility in the ECB pattern.</p>
 *
 * <p>The validation is done here so that it may be used by other Boundary Resources. Other Business Logic would go here
 * as well.</p>
 *
 * <p>There are no access modifiers on the methods, making them 'package' scope.  They should only be accessed by a
 * Boundary / Web Service class with public methods.</p>
 *
 * @author Joshua Wilson
 * @see TaxiValidator
 * @see TaxiRepository
 */

//@Dependent annotation designates the default scope, listed here so that you know what scope is being used.
@Dependent
public class TaxiService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private TaxiValidator validator;

    @Inject
    private TaxiRepository crud;

    @Inject
    private @Named("httpClient") CloseableHttpClient httpClient;
    
    /**
     * <p>Returns a List of all persisted {@link Taxi} objects, sorted alphabetically by registration name.<p/>
     * 
     * @return List of Taxi objects
     */
    List<Taxi> findAllOrderedByRegistration() {
        return crud.findAllOrderedByRegistration();
    }

    /**
     * <p>Returns a single Taxi object, specified by a Long id.<p/>
     * 
     * @param id The id field of the Taxi to be returned
     * @return The Taxi with the specified id
     */
    Taxi findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single Taxi object, specified by a String email.</p>
     *
     * <p>If there is more than one Taxi with the specified email, only the first encountered will be returned.<p/>
     * 
     * @param registration The registration field of the Taxi to be returned
     * @return The first Taxi with the specified registration
     */
    Taxi findByRegistration(String registration) {
        return crud.findByRegistration(registration);
    }

    /**
     * <p>Returns a single Taxi object, specified by a String seat number.<p/>
     *
     * <p>If there is more then one, only the first will be returned.<p/>
     * 
     * @param seat The seat field of the Taxi to be returned
     * @return The first Taxi with the specified seat
     */
    Taxi findBySeat(String seat) {
        return crud.findBySeat(seat);
    }


    /**
     * <p>Writes the provided Taxi object to the application database.<p/>
     *
     * <p>Validates the data in the provided Taxi object using a {@link TaxiValidator} object.<p/>
     * 
     * @param taxi The Taxi object to be written to the database using a {@link TaxiRepository} object
     * @return The Taxi object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Taxi create(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiService.create() - Creating " + taxi.getRegistration());
        
        // Check to make sure the data fits with the parameters in the Taxi model and passes validation.
        validator.validateTaxi(taxi);
        
/* detect the phone number belongs to which region through an outside API
        //Perform a rest call to get the state of the taxi from the allareacodes.com API
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.allareacodes.com")
                .setPath("/api/1.0/api.json")
                .setParameter("npa", taxi.getPhoneNumber().substring(1,4))
                .setParameter("tracking_email", "h.firth@ncl.ac.uk")
                .setParameter("tracking_url", "http://www.ncl.ac.uk/undergraduate/modules/module/CSC8104")
                .build();
        HttpGet req = new HttpGet(uri);
        CloseableHttpResponse response = httpClient.execute(req);
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray areaCodes = responseJson.getJSONArray("area_codes");
        taxi.setState(areaCodes.getJSONObject(0).getString("state"));
        HttpClientUtils.closeQuietly(response);
*/

        // Write the taxi to the database.
        return crud.create(taxi);
    }

    /**
     * <p>Updates an existing Taxi object in the application database with the provided Taxi object.<p/>
     *
     * <p>Validates the data in the provided Taxi object using a TaxiValidator object.<p/>
     * 
     * @param taxi The Taxi object to be passed as an update to the application database
     * @return The Taxi object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Taxi update(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiService.update() - Updating " + taxi.getRegistration());
        
        // Check to make sure the data fits with the parameters in the taxi model and passes validation.
        validator.validateTaxi(taxi);
/* update the phone number belongs to which region through an outside API
        //Perform a rest call to get the state of the taxi from the allareacodes.com API
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.allareacodes.com")
                .setPath("/api/1.0/api.json")
                .setParameter("npa", taxi.getPhoneNumber().substring(1,4))
                .setParameter("tracking_email", "h.firth@ncl.ac.uk")
                .setParameter("tracking_url", "http://www.ncl.ac.uk/undergraduate/modules/module/CSC8104")
                .build();
        HttpGet req = new HttpGet(uri);
        CloseableHttpResponse response = httpClient.execute(req);
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray areaCodes = responseJson.getJSONArray("area_codes");
        taxi.setState(areaCodes.getJSONObject(0).getString("state"));
        HttpClientUtils.closeQuietly(response);
*/
        // Either update the taxi or add it if it can't be found.
        return crud.update(taxi);
    }

}
