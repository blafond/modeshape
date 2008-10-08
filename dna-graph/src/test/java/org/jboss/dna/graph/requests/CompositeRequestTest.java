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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Randall Hauch
 */
public class CompositeRequestTest extends AbstractRequestTest {

    private Request request;
    private Request[] requests;
    private List<Request> requestList;

    @Override
    @Before
    public void beforeEach() {
        super.beforeEach();
        Request request1 = new ReadPropertyRequest(validPathLocation1, createName("property"));
        Request request2 = new ReadPropertyRequest(validPathLocation2, createName("property"));
        Request request3 = new ReadAllChildrenRequest(validPathLocation);
        requests = new Request[] {request1, request2, request3};
        requestList = Arrays.asList(requests);
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCreatingCompositeRequestWithNullRequest() {
        CompositeRequest.with((Request)null);
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCreatingCompositeRequestWithNullRequestArray() {
        CompositeRequest.with((Request[])null);
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCreatingCompositeRequestWithNullRequestIterator() {
        CompositeRequest.with((Iterator<Request>)null);
    }

    @Test
    public void shouldReturnRequestWhenCreatingCompositeFromSingleRequest() {
        request = CompositeRequest.with(requests[0]);
        assertThat(request, is(sameInstance(requests[0])));
    }

    @Test
    public void shouldReturnRequestWhenCreatingCompositeFromIteratorOverSingleRequest() {
        requestList = Collections.singletonList(requests[0]);
        request = CompositeRequest.with(requestList.iterator());
        assertThat(request, is(sameInstance(requestList.get(0))));
    }

    @Test
    public void shouldCreateCompositeFromMultipleRequests() {
        request = CompositeRequest.with(requests);
        assertThat(request, is(instanceOf(CompositeRequest.class)));
        CompositeRequest composite = (CompositeRequest)request;
        assertThat(composite.size(), is(3));
        assertThat(composite.size(), is(requests.length));
        assertThat(composite.getRequests(), hasItems(requests));
        Iterator<Request> actual = composite.iterator();
        Iterator<Request> expected = requestList.iterator();
        while (actual.hasNext() && expected.hasNext()) {
            assertThat(actual.next(), is(sameInstance(expected.next())));
        }
        assertThat(actual.hasNext(), is(expected.hasNext()));
        assertThat(composite.hasError(), is(false));
    }

    @Test
    public void shouldCreateCompositeFromIteratorOverRequests() {
        request = CompositeRequest.with(requestList.iterator());
        assertThat(request, is(instanceOf(CompositeRequest.class)));
        CompositeRequest composite = (CompositeRequest)request;
        assertThat(composite.size(), is(3));
        assertThat(composite.size(), is(requestList.size()));
        assertThat(composite.getRequests(), hasItems(requests));
        Iterator<Request> actual = composite.iterator();
        Iterator<Request> expected = requestList.iterator();
        while (actual.hasNext() && expected.hasNext()) {
            assertThat(actual.next(), is(sameInstance(expected.next())));
        }
        assertThat(actual.hasNext(), is(expected.hasNext()));
        assertThat(composite.hasError(), is(false));
    }

    @Test
    public void shouldConsiderTwoCompositesOfSameRequestsToBeEqual() {
        request = CompositeRequest.with(requests);
        Request request2 = CompositeRequest.with(requests);
        assertThat(request, is(request2));
        assertThat(request.hasError(), is(false));
    }

}
