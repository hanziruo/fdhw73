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

import javax.inject.Inject;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This is a Repository class and connects the Service/Control layer (see {@link TaxiService} with the
 * Domain/Entity Object (see {@link Taxi}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 * 
 * @author Yuanhui Lu
 * @see Taxi
 * @see javax.persistence.EntityManager
 */
public class TaxiRepository {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link Taxi} objects, sorted alphabetically by registration name.</p>
     * 
     * @return List of Taxi objects
     */
    List<Taxi> findAllOrderedByRegistration() {
        TypedQuery<Taxi> query = em.createNamedQuery(Taxi.FIND_ALL, Taxi.class); 
        return query.getResultList();
    }

    /**
     * <p>Returns a single Taxi object, specified by a Long id.<p/>
     *
     * @param id The id field of the Taxi to be returned
     * @return The Taxi with the specified id
     */
    Taxi findById(Long id) {
        return em.find(Taxi.class, id);
    }

    /**
     * <p>Returns a single Taxi object, specified by a String registration.<p/>
     *
     * <p>If there is more then one, only the first will be returned.<p/>
     *
     * @param registration The registration field of the Taxi to be returned
     * @return The first Taxi with the specified registration
     */
    Taxi findByRegistration(String registration) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Taxi> criteria = cb.createQuery(Taxi.class);
        Root<Taxi> taxi = criteria.from(Taxi.class);
        // Swap criteria statements if you would like to try out type-safe criteria queries, a new feature in JPA 2.0.
        // criteria.select(taxi).where(cb.equal(taxi.get(Taxi_.registration), registration));
        criteria.select(taxi).where(cb.equal(taxi.get("registration"), registration));
        return em.createQuery(criteria).getSingleResult();
    }
    
    /**
     * <p>Returns a single Taxi object, specified by a String seat.</p>
     *
     * <p>If there is more than one Taxi with the specified seat number, only the first encountered will be returned.<p/>
     *
     * @param seat The seat field of the Taxi to be returned
     * @return The first Taxi with the specified seat number
     */
    Taxi findBySeat(String seat) {
        TypedQuery<Taxi> query = em.createNamedQuery(Taxi.FIND_BY_SEAT, Taxi.class).setParameter("seat", seat); 
        return query.getSingleResult();
    }
    
   
   

    /**
     * <p>Persists the provided Taxi object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param taxi The Taxi object to be persisted
     * @return The Taxi object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    
   
    Taxi create(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiRepository.create() - Creating " + taxi.getRegistration());
        
        // Write the taxi to the database.
        em.persist(taxi);
        
        return taxi;
    }

    /**
     * <p>Updates an existing Taxi object in the application database with the provided Taxi object.</p>
     * 
     * <p>{@link javax.persistence.EntityManager#merge(Object) merge(Object)} creates a new instance of your entity,
     * copies the state from the supplied entity, and makes the new copy managed. The instance you pass in will not be
     * managed (any changes you make will not be part of the transaction - unless you call merge again).</p>
     * 
     * <p>merge(Object) however must have an object with the @Id already generated.</p>
     * 
     * @param taxi The Taxi object to be merged with an existing Taxi
     * @return The Taxi that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Taxi update(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiRepository.update() - Updating " + taxi.getRegistration());
        
        // Either update the taxi or add it if it can't be found.
        em.merge(taxi);
        
        return taxi;
    }
    
   

}
