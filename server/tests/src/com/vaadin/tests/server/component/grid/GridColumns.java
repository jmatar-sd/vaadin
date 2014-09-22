/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.server.component.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.KeyMapper;
import com.vaadin.shared.ui.grid.GridColumnState;
import com.vaadin.shared.ui.grid.GridState;
import com.vaadin.ui.components.grid.Grid;
import com.vaadin.ui.components.grid.GridColumn;

public class GridColumns {

    private Grid grid;

    private GridState state;

    private Method getStateMethod;

    private Field columnIdGeneratorField;

    private KeyMapper<Object> columnIdMapper;

    @Before
    public void setup() throws Exception {
        IndexedContainer ds = new IndexedContainer();
        for (int c = 0; c < 10; c++) {
            ds.addContainerProperty("column" + c, String.class, "");
        }
        grid = new Grid(ds);

        getStateMethod = Grid.class.getDeclaredMethod("getState");
        getStateMethod.setAccessible(true);

        state = (GridState) getStateMethod.invoke(grid);

        columnIdGeneratorField = Grid.class.getDeclaredField("columnKeys");
        columnIdGeneratorField.setAccessible(true);

        columnIdMapper = (KeyMapper<Object>) columnIdGeneratorField.get(grid);
    }

    @Test
    public void testColumnGeneration() throws Exception {

        for (Object propertyId : grid.getContainerDatasource()
                .getContainerPropertyIds()) {

            // All property ids should get a column
            GridColumn column = grid.getColumn(propertyId);
            assertNotNull(column);

            // Property id should be the column header by default
            assertEquals(propertyId.toString(), grid.getHeader()
                    .getDefaultRow().getCell(propertyId).getText());
        }
    }

    @Test
    public void testModifyingColumnProperties() throws Exception {

        // Modify first column
        GridColumn column = grid.getColumn("column1");
        assertNotNull(column);

        column.setHeaderCaption("CustomHeader");
        assertEquals("CustomHeader", column.getHeaderCaption());
        assertEquals(column.getHeaderCaption(), grid.getHeader()
                .getDefaultRow().getCell("column1").getText());

        column.setVisible(false);
        assertFalse(column.isVisible());
        assertFalse(getColumnState("column1").visible);

        column.setVisible(true);
        assertTrue(column.isVisible());
        assertTrue(getColumnState("column1").visible);

        column.setWidth(100);
        assertEquals(100, column.getWidth());
        assertEquals(column.getWidth(), getColumnState("column1").width);

        try {
            column.setWidth(-1);
            fail("Setting width to -1 should throw exception");
        } catch (IllegalArgumentException iae) {

        }

        assertEquals(100, column.getWidth());
        assertEquals(100, getColumnState("column1").width);
    }

    @Test
    public void testRemovingColumn() throws Exception {

        GridColumn column = grid.getColumn("column1");
        assertNotNull(column);

        // Remove column
        grid.getContainerDatasource().removeContainerProperty("column1");

        try {
            column.setHeaderCaption("asd");

            fail("Succeeded in modifying a detached column");
        } catch (IllegalStateException ise) {
            // Detached state should throw exception
        }

        try {
            column.setVisible(false);
            fail("Succeeded in modifying a detached column");
        } catch (IllegalStateException ise) {
            // Detached state should throw exception
        }

        try {
            column.setWidth(123);
            fail("Succeeded in modifying a detached column");
        } catch (IllegalStateException ise) {
            // Detached state should throw exception
        }

        assertNull(grid.getColumn("column1"));
        assertNull(getColumnState("column1"));
    }

    @Test
    public void testAddingColumn() throws Exception {
        grid.getContainerDatasource().addContainerProperty("columnX",
                String.class, "");
        GridColumn column = grid.getColumn("columnX");
        assertNotNull(column);
    }

    @Test
    public void testHeaderVisiblility() throws Exception {

        assertTrue(grid.getHeader().isVisible());
        assertTrue(state.header.visible);

        grid.getHeader().setVisible(false);
        assertFalse(grid.getHeader().isVisible());
        assertFalse(state.header.visible);

        grid.getHeader().setVisible(true);
        assertTrue(grid.getHeader().isVisible());
        assertTrue(state.header.visible);
    }

    @Test
    public void testFooterVisibility() throws Exception {

        assertTrue(grid.getFooter().isVisible());
        assertTrue(state.footer.visible);

        grid.getFooter().setVisible(false);
        assertFalse(grid.getFooter().isVisible());
        assertFalse(state.footer.visible);

        grid.getFooter().setVisible(true);
        assertTrue(grid.getFooter().isVisible());
        assertTrue(state.footer.visible);
    }

    @Test
    public void testFrozenColumnByPropertyId() {
        assertNull("Grid should not start with a frozen column",
                grid.getLastFrozenPropertyId());

        Object propertyId = grid.getContainerDatasource()
                .getContainerPropertyIds().iterator().next();
        grid.setLastFrozenPropertyId(propertyId);
        assertEquals(propertyId, grid.getLastFrozenPropertyId());

        grid.getContainerDatasource().removeContainerProperty(propertyId);
        assertNull(grid.getLastFrozenPropertyId());
    }

    private GridColumnState getColumnState(Object propertyId) {
        String columnId = columnIdMapper.key(propertyId);
        for (GridColumnState columnState : state.columns) {
            if (columnState.id.equals(columnId)) {
                return columnState;
            }
        }
        return null;
    }

}