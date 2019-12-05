/*
 * ExportScheduleTableModel.java
 *
 * Created on 4 februari 2003, 10:25
 */

package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.core.Translator;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.actions.TableSortSupport;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Karel
 */
public class AspectTableModel<T> extends javax.swing.table.AbstractTableModel implements TableSortSupport {

    public final static String BUSINESSOBJECT_ASPECT = "businessObject";

    private List<T> models = new ArrayList<>();
    private List<PropertyDescriptor> descriptors = new ArrayList<>();
    private boolean showSequenceNumbers = false;
    // indices of the columns NOT to sort on:
    private HashSet<Integer> unsortableColumns = new HashSet<>();
    // new Flag: setMissingResourcesFlag -
    // should the 'MR' (missing resource) indicator being used in the table header Yes/No
    protected boolean missingResourcesFlag = true;

    /**
     * Creates a new instance of ExportScheduleTableModel
     */
    public AspectTableModel() {
    }

    public AspectTableModel(List<T> models) {
        this.models = models;
    }

    public AspectTableModel(List<T> models, List<PropertyDescriptor> descriptors) {
        this(models);
        this.descriptors = descriptors;
    }

    public void setMissingResourcesFlag(boolean flag) {
        this.missingResourcesFlag = flag;
    }

    protected List<PropertyDescriptor> getDescriptors() {
        return descriptors;
    }

    protected PropertyDescriptor getDescriptor(String aspect) {
        for (PropertyDescriptor each : descriptors) {
            if (each.getName().equals(aspect)) {
                return each;
            }
        }
        return null;
    }

    protected PropertyDescriptor getDescriptor(int index) {
        index = showSequenceNumbers ? index - 1 : index;
        return descriptors.get(index);
    }

    public List<T> getModels() {
        return models;
    }

    public void setModels(List<T> models) {
        this.models = models;
        fireTableDataChanged();
    }

    protected T getModel(int index) {
        return models.get(index);
    }

    public void add(T model) {
        int range = getRowCount();
        if (models.add(model)) {
            fireTableRowsInserted(range, range);
        }
    }

    public void add(int index, T model) {
        models.add(index, model);
        fireTableRowsInserted(index, index);
    }

    public void remove(int index) {
        if (index >= 0 && index < getRowCount()) {
            models.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void remove(T model) {
        this.remove(models.indexOf(model));
    }

    public void removeAll(Collection<T> modelsToRemove) {
        if (modelsToRemove.isEmpty()) {
            return;
        }
        if (models.removeAll(modelsToRemove)) {
            fireTableDataChanged();
        }
    }

    public void removeAll() { // gde
        if (models.isEmpty()) {
            return; // nothing to remove
        }
        int i = getRowCount();
        models.clear();
        fireTableRowsDeleted(0, i - 1);
    }

    public void addDescriptor(PropertyDescriptor descriptor) {
        this.descriptors.add(descriptor);
    }

    public void addAll(Collection<T> models) {
        if (models.isEmpty()) {
            return; // nothing to add
        }
        int iRange = getRowCount();
        if (this.models.addAll(models)) {
            fireTableRowsInserted(iRange, getRowCount() - 1);
        }
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
        return models.size();
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
        int result = descriptors.size();
        return showSequenceNumbers ? result + 1 : result;
    }

    public String getColumnName(int index) {
        if (showSequenceNumbers && index == 0) {
            return "#";
        } else {
            return translator().getTranslation(getDescriptor(index).getDisplayName(), missingResourcesFlag);
        }
    }

    protected Translator translator() {
        return TranslatorProvider.instance.get().getTranslator();
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
        if (showSequenceNumbers && columnIndex == 0) {
            return rowIndex + 1;
        }
        T model = models.get(rowIndex);
        Method getter = getDescriptor(columnIndex).getReadMethod();
        if (model == null || getter == null) {
            return null;
        }
        try {
            return getter.invoke(model, null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Class getColumnClass(int columnIndex) {
        if (showSequenceNumbers && columnIndex == 0) {
            return Long.class;
        }
        Class result = getDescriptor(columnIndex).getPropertyType();
        if (result.isPrimitive()) {
            if (result == Boolean.TYPE) {
                return Boolean.class;
            }
            if (result == Integer.TYPE) {
                return Integer.class;
            }
            if (result == Long.TYPE) {
                return Long.class;
            }
            if (result == Short.TYPE) {
                return Short.class;
            }
            if (result == Byte.TYPE) {
                return Byte.class;
            }
            if (result == Character.TYPE) {
                return Character.class;
            }
            if (result == Float.TYPE) {
                return Float.class;
            }
            if (result == Double.TYPE) {
                return Double.class;
            }
        }
        return result;

    }

    // gde

    public T getObjectAt(int rowIndex) {
        return getModel(rowIndex);
    }

    public boolean getShowSequenceNumbers() {
        return showSequenceNumbers;
    }

    public void setShowSequenceNumbers(boolean showSequenceNumbers) {
        this.showSequenceNumbers = showSequenceNumbers;
    }

    public void setColumnSortable(int colIndex, boolean sortable) {
        if (unsortableColumns.contains(colIndex)) {
            if (sortable) {
                unsortableColumns.remove(colIndex);
            }
        } else if (!sortable) {
            unsortableColumns.add(colIndex);
        }
    }

    // TableSortSupport interface

    public boolean isColumnSortable(int columnIndex) {
        return !unsortableColumns.contains(columnIndex);
    }

}
    