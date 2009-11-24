/*
 * JBoss DNA (http://www.jboss.org/dna)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * JBoss DNA is free software. Unless otherwise indicated, all code in JBoss DNA
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * JBoss DNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.graph.query.process;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import org.jboss.dna.graph.ExecutionContext;
import org.jboss.dna.graph.Location;
import org.jboss.dna.graph.property.Path;
import org.jboss.dna.graph.property.PropertyType;
import org.jboss.dna.graph.query.AbstractQueryTest;
import org.jboss.dna.graph.query.QueryResults.Columns;
import org.jboss.dna.graph.query.model.Column;
import org.jboss.dna.graph.query.model.FullTextSearchScore;
import org.jboss.dna.graph.query.model.Length;
import org.jboss.dna.graph.query.model.NodeDepth;
import org.jboss.dna.graph.query.model.NodeLocalName;
import org.jboss.dna.graph.query.model.NodeName;
import org.jboss.dna.graph.query.model.NodePath;
import org.jboss.dna.graph.query.model.Order;
import org.jboss.dna.graph.query.model.Ordering;
import org.jboss.dna.graph.query.model.PropertyValue;
import org.jboss.dna.graph.query.model.SelectorName;
import org.jboss.dna.graph.query.model.TypeSystem;
import org.jboss.dna.graph.query.validate.ImmutableSchemata;
import org.jboss.dna.graph.query.validate.Schemata;

/**
 * 
 */
public abstract class AbstractQueryResultsTest extends AbstractQueryTest {

    protected ExecutionContext executionContext = ExecutionContext.DEFAULT_CONTEXT;
    protected TypeSystem typeSystem = executionContext.getValueFactories().getTypeSystem();

    protected Path path( String name ) {
        return executionContext.getValueFactories().getPathFactory().create(name);
    }

    protected Schemata schemataFor( Columns columns ) {
        return schemataFor(columns, new PropertyType[] {});
    }

    protected Schemata schemataFor( Columns columns,
                                    PropertyType... types ) {
        ImmutableSchemata.Builder builder = ImmutableSchemata.createBuilder(typeSystem);
        for (String selectorName : columns.getSelectorNames()) {
            final SelectorName selector = selector(selectorName);
            int i = 0;
            for (Column column : columns.getColumns()) {
                final String name = column.getColumnName();
                final PropertyType type = types != null && types.length > i && types[i] != null ? types[i] : PropertyType.STRING;
                if (column.getSelectorName().equals(selector)) {
                    builder.addColumn(selectorName, name, type.getName().toUpperCase());
                    ++i;
                }
            }
        }
        return builder.build();
    }

    protected Columns resultColumns( String selectorName,
                                     String... columnNames ) {
        // Define the columns ...
        List<Column> columnObj = new ArrayList<Column>();
        SelectorName selector = selector(selectorName);
        for (String columnName : columnNames) {
            columnObj.add(new Column(selector, columnName, columnName));
        }
        return new QueryResultColumns(columnObj, false);
    }

    protected Columns resultColumnsWithSearchResults( String selectorName,
                                                      String... columnNames ) {
        // Define the columns ...
        List<Column> columnObj = new ArrayList<Column>();
        SelectorName selector = selector(selectorName);
        for (String columnName : columnNames) {
            columnObj.add(new Column(selector, columnName, columnName));
        }
        return new QueryResultColumns(columnObj, true);
    }

    protected Object[] tuple( Columns columns,
                              String path,
                              Object... values ) {
        return tuple(columns, path(path), values);
    }

    protected Object[] tuple( Columns columns,
                              Path path,
                              Object... values ) {
        return tuple(columns, Location.create(path), values);
    }

    protected Object[] tuple( Columns columns,
                              String[] paths,
                              Object... values ) {
        Location[] locations = new Location[paths.length];
        for (int i = 0; i != paths.length; ++i) {
            locations[i] = Location.create(path(paths[i]));
        }
        return tuple(columns, locations, values);
    }

    protected Object[] tuple( Columns columns,
                              Path[] paths,
                              Object... values ) {
        Location[] locations = new Location[paths.length];
        for (int i = 0; i != paths.length; ++i) {
            locations[i] = Location.create(paths[i]);
        }
        return tuple(columns, locations, values);
    }

    protected Object[] tuple( Columns columns,
                              Location location,
                              Object... values ) {
        return tuple(columns, new Location[] {location}, values);
    }

    protected Object[] tuple( Columns columns,
                              Location[] locations,
                              Object... values ) {
        Object[] results = new Object[columns.getTupleSize()];
        // Set the column values ...
        assertThat(values.length, is(columns.getColumnCount()));
        for (int i = 0; i != columns.getColumnCount(); ++i) {
            results[i] = values[i];
        }
        // Set the location ...
        int index = columns.getColumnCount();
        for (Location location : locations) {
            results[index++] = location;
        }
        // Set the full-text-search results ...
        if (columns.hasFullTextSearchScores()) {
            results[index++] = 1.0;
        }
        return results;
    }

    protected Object[] tuple( Columns columns,
                              Location[] locations,
                              double[] scores,
                              Object... values ) {
        assertThat(values.length, is(columns.getColumnCount()));
        assertThat(locations.length, is(columns.getLocationCount()));
        assertThat(scores.length, is(columns.getLocationCount()));
        Object[] results = new Object[columns.getTupleSize()];
        // Set the column values ...
        for (int i = 0; i != columns.getColumnCount(); ++i) {
            results[i] = values[i];
        }
        // Set the location ...
        int index = columns.getColumnCount();
        for (Location location : locations) {
            results[index++] = location;
        }
        // Set the full-text-search results ...
        if (columns.hasFullTextSearchScores()) {
            for (double score : scores) {
                results[index++] = score;
            }
        }
        return results;
    }

    protected Ordering orderByPropertyValue( Column column ) {
        return orderByPropertyValue(column, Order.ASCENDING);
    }

    protected Ordering orderByPropertyValue( Column column,
                                             Order order ) {
        return new Ordering(new PropertyValue(column.getSelectorName(), column.getPropertyName()), order);
    }

    protected Ordering orderByPropertyLength( Column column ) {
        return orderByPropertyValue(column, Order.ASCENDING);
    }

    protected Ordering orderByPropertyLength( Column column,
                                              Order order ) {
        return new Ordering(new Length(new PropertyValue(column.getSelectorName(), column.getPropertyName())), order);
    }

    protected Ordering orderByNodeDepth( String selectorName ) {
        return orderByNodeDepth(selectorName, Order.ASCENDING);
    }

    protected Ordering orderByNodeDepth( String selectorName,
                                         Order order ) {
        return new Ordering(new NodeDepth(selector(selectorName)), order);
    }

    protected Ordering orderByNodePath( String selectorName ) {
        return orderByNodePath(selectorName, Order.ASCENDING);
    }

    protected Ordering orderByNodePath( String selectorName,
                                        Order order ) {
        return new Ordering(new NodePath(selector(selectorName)), order);
    }

    protected Ordering orderByNodeName( String selectorName ) {
        return orderByNodeName(selectorName, Order.ASCENDING);
    }

    protected Ordering orderByNodeName( String selectorName,
                                        Order order ) {
        return new Ordering(new NodeName(selector(selectorName)), order);
    }

    protected Ordering orderByNodeLocalName( String selectorName ) {
        return orderByNodeLocalName(selectorName, Order.ASCENDING);
    }

    protected Ordering orderByNodeLocalName( String selectorName,
                                             Order order ) {
        return new Ordering(new NodeLocalName(selector(selectorName)), order);
    }

    protected Ordering orderByFullTextSearchScore( String selectorName ) {
        return orderByFullTextSearchScore(selectorName, Order.ASCENDING);
    }

    protected Ordering orderByFullTextSearchScore( String selectorName,
                                                   Order order ) {
        return new Ordering(new FullTextSearchScore(selector(selectorName)), order);
    }

}
