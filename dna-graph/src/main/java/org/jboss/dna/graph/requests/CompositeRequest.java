/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.graph.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jboss.dna.common.util.CheckArg;

/**
 * A request that wraps multiple other requests.
 * 
 * @author Randall Hauch
 */
public class CompositeRequest extends Request implements Iterable<Request> {

    /**
     * Return a request that either wraps multiple requests, or the single request if only one is supplied.
     * 
     * @param requests the requests to wrap
     * @return the requests wrapped in a CompositeRequest, or if only one request is supplied that single request
     * @throws IllegalArgumentException if there requests are null, empty, or contains only nulls
     */
    public static Request with( Request... requests ) {
        CheckArg.isNotEmpty(requests, "requests");
        if (requests.length == 1) {
            CheckArg.isNotNull(requests[0], "requests[0]");
            return requests[0];
        }
        List<Request> list = new ArrayList<Request>(requests.length);
        for (Request request : requests) {
            if (request == null) continue;
            if (request instanceof CompositeRequest) {
                CompositeRequest composite = (CompositeRequest)request;
                list.addAll(composite.getRequests());
            } else {
                list.add(request);
            }
        }
        CheckArg.isNotEmpty(list, "requests");
        return new CompositeRequest(list);
    }

    /**
     * Return a request that either wraps multiple requests, or the single request if only one is supplied.
     * 
     * @param requests the requests to wrap
     * @return the requests wrapped in a CompositeRequest, or if only one request is supplied that single request
     * @throws IllegalArgumentException if there requests are null, empty, or contains only nulls
     */
    public static Request with( Iterator<Request> requests ) {
        CheckArg.isNotNull(requests, "requests");
        List<Request> list = new LinkedList<Request>();
        while (requests.hasNext()) {
            Request request = requests.next();
            if (request == null) continue;
            if (request instanceof CompositeRequest) {
                CompositeRequest composite = (CompositeRequest)request;
                list.addAll(composite.getRequests());
            } else {
                list.add(request);
            }
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        CheckArg.isNotEmpty(list, "requests");
        return new CompositeRequest(list);
    }

    /**
     * Add requests to the supplied composite request.
     * 
     * @param composite the composite request to which the requests are to be added
     * @param requests the requests to wrap
     * @return the requests wrapped in a CompositeRequest, or if only one request is supplied that single request, or null if
     *         there are no request
     * @throws IllegalArgumentException if the composite request is null
     */
    public static CompositeRequest add( CompositeRequest composite,
                                        Request... requests ) {
        CheckArg.isNotNull(composite, "composite");
        if (requests == null || requests.length == 0) return composite;
        List<Request> list = new ArrayList<Request>(requests.length + composite.size());
        if (composite.size() != 0) list.addAll(composite.getRequests());
        for (Request request : requests) {
            if (request == null) continue;
            if (request instanceof CompositeRequest) {
                CompositeRequest compositeRequest = (CompositeRequest)request;
                list.addAll(compositeRequest.getRequests());
            } else {
                list.add(request);
            }
        }
        return new CompositeRequest(list);
    }

    /**
     * Add requests to the supplied composite request.
     * 
     * @param composite the composite request to which the requests are to be added
     * @param requests the requests to wrap
     * @return the requests wrapped in a CompositeRequest, or if only one request is supplied that single request, or null if
     *         there are no request
     * @throws IllegalArgumentException if the composite request is null
     */
    public static CompositeRequest add( CompositeRequest composite,
                                        Iterator<Request> requests ) {
        CheckArg.isNotNull(composite, "composite");
        List<Request> list = new LinkedList<Request>();
        if (composite.size() != 0) list.addAll(composite.getRequests());
        while (requests.hasNext()) {
            Request request = requests.next();
            if (request == null) continue;
            if (request instanceof CompositeRequest) {
                CompositeRequest compositeRequest = (CompositeRequest)request;
                list.addAll(compositeRequest.getRequests());
            } else {
                list.add(request);
            }
        }
        return new CompositeRequest(list);
    }

    private final List<Request> requests;

    /**
     * Create a composite request from the supplied list of requests.
     * 
     * @param requests the modifiable list of requests; may not be null
     */
    protected CompositeRequest( List<Request> requests ) {
        this.requests = Collections.unmodifiableList(requests);
    }

    /**
     * Return the unmodifiable requests contained in this composite request.
     * 
     * @return requests
     */
    public List<Request> getRequests() {
        return requests;
    }

    /**
     * Get the number of requests.
     * 
     * @return the number of requests
     */
    public int size() {
        return requests.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Request> iterator() {
        return requests.iterator();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if (obj instanceof CompositeRequest) {
            CompositeRequest that = (CompositeRequest)obj;
            if (this.size() != that.size()) return false;
            Iterator<Request> thisIter = this.iterator();
            Iterator<Request> thatIter = that.iterator();
            while (thisIter.hasNext()) {
                Request thisRequest = thisIter.next();
                Request thatRequest = thatIter.next();
                if (thisRequest == null) {
                    if (thatRequest != null) return false;
                } else {
                    if (!thisRequest.equals(thatRequest)) return false;
                }
            }
            return true;
        }
        return false;
    }

}
