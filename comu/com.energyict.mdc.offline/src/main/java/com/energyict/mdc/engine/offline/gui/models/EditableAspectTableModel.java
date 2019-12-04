/*
 * EditableAspectTableModel.java
 *
 * Created on 10 februari 2003, 18:15
 */

package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.event.TableModelEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Karel
 */
public class EditableAspectTableModel<T> extends AspectTableModel<T> {

    private Set<String> readOnlyAspects = new HashSet<String>();

    /**
     * Creates a new instance of EditableAspectTableModel
     */
    public EditableAspectTableModel() {
    }

    public EditableAspectTableModel(List<T> models) {
        super(models);
    }

    public EditableAspectTableModel(List<T> models, List<PropertyDescriptor> descriptors) {
        super(models, descriptors);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (getShowSequenceNumbers() && columnIndex == 0) {
            return false;
        }
        PropertyDescriptor descriptor = getDescriptor(columnIndex);
        if (readOnlyAspects.contains(descriptor.getName())) {
            return false;
        }
        if (getDescriptor(columnIndex).getWriteMethod() == null) {
            return false;
        }
        Class classType = getDescriptor(columnIndex).getPropertyType();
        if (classType.isPrimitive()) {
            return true;
        }
        if (classType == String.class) {
            return true;
        }
        if (classType == Date.class) {
            return true;
        }
        if (classType == BigDecimal.class) {
            return true;
        }
        return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        T model = getModel(rowIndex);
        Method setter = getDescriptor(columnIndex).getWriteMethod();
        try {
            Object[] args = new Object[1];
            args[0] = aValue;
            setter.invoke(model, args);

            fireTableChanged(new TableModelEvent(
                    this,
                    rowIndex,
                    rowIndex,
                    columnIndex,
                    TableModelEvent.UPDATE));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setReadOnly(String aspect, boolean readOnly) {
        if (readOnly) {
            readOnlyAspects.add(aspect);
        } else {
            readOnlyAspects.remove(aspect);
        }
    }

}

