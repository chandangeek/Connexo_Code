/*
 * PropertiesTableModel.java
 *
 * Created on 4 februari 2003, 10:25
 */

package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.core.Association;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.upl.TypedProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Karel
 */
public class PropertiesTableModel extends javax.swing.table.AbstractTableModel {

    private String columnNames[] = {"name", "value"}; // gde (2003-jul-03)
    private TypedPropertiesOwner model;
    private List<Association> associations;
    private boolean checkForValidKeysOnly = false;

    /**
     * Creates a new instance of ExportScheduleTableModel
     */
    public PropertiesTableModel(TypedPropertiesOwner model) {
        this.model = model;
        setProperties(model.getProperties());
    }

    public PropertiesTableModel(TypedPropertiesOwner model,
                                String columnNames[]) { // gde (2003-jul-03)
        this(model);
        this.columnNames = columnNames;
    }

    public TypedProperties getProperties() {
        TypedProperties result = TypedProperties.empty();
        Iterator it = associations.iterator();
        while (it.hasNext()) {
            Association association = (Association) it.next();
            if (checkForValidKeysOnly) {
                if (association.hasValidKey()) {
                    String strValue = association.getValue() == null ?
                            "" : association.getValue(); // null not allowed in Properties
                    result.setProperty(association.getKey(), strValue);
                }
            } else if (association.isValid()) {
                result.setProperty(association.getKey(), association.getValue());
            }
        }
        return result;
    }

    private void setProperties(TypedProperties properties) {
        List<String> propertyNames = new ArrayList<>(properties.localPropertyNames());
        Collections.sort(propertyNames);
        this.associations = new ArrayList<>(propertyNames.size());
        for (String propertyName : propertyNames) {
            Association association = new Association(propertyName, (String) properties.getProperty(propertyName));
            this.associations.add(association);
        }
    }

    public void add() {
        int range = associations.size();
        associations.add(new Association(null, null));
        fireTableRowsInserted(range, range);
    }

    public void remove(int index) {
        associations.remove(index);
        TypedProperties newProps = getProperties();
        if (!model.getProperties().equals(newProps)) {
            model.setProperties(newProps);
        }
        fireTableRowsDeleted(index, index);
    }

    public void remove(int[] indices) {
        List<Association> toRemove = new ArrayList();
        int min = Integer.MAX_VALUE;
        int max = -1;
        for (int index : indices) {
            toRemove.add(associations.get(index));
            min = Math.min(min, index);
            max = Math.max(max, index);
        }
        associations.removeAll(toRemove);
        TypedProperties newProps = getProperties();
        if (!model.getProperties().equals(newProps)) {
            model.setProperties(newProps);
        }
        fireTableRowsDeleted(min, max);
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        return associations.size();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int index) {
        switch (index) {
            case 0:
            case 1:
                return TranslatorProvider.instance.get().getTranslator().getTranslation(columnNames[index]);
            default:
                return "?";
        }
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Association association = (Association) associations.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return association.getKey();
            case 1:
                return association.getValue();
            default:
                return null;
        }
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= associations.size()) {
            return;
        }
        Association association = (Association) associations.get(rowIndex);
        switch (columnIndex) {
            case 0:
                association.setKey(aValue == null ? null : aValue.toString());
                break;
            case 1:
                association.setValue(aValue == null ? null : aValue.toString());
                break;
        }
        TypedProperties newProps = getProperties();
        if (!model.getProperties().equals(newProps)) {
            model.setProperties(newProps);
        }
        fireTableCellUpdated(rowIndex, columnIndex); // gde (2003-jul-24)
    }

    public void setCheckForValidKeysOnly(boolean value) {
        this.checkForValidKeysOnly = value;
    }
}
