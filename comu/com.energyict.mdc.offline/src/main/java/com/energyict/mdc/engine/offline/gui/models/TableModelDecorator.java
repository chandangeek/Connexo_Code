package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

// TableModelDecorator implements TableModelListener. That
// listener interface defines one method: tableChanged(), which

// is called when the table model is changed. That method is
// not implemented in this abstract class; it's left for
// subclasses to implement.
public abstract class TableModelDecorator implements TableModel, TableModelListener {

    private TableModel realModel; // We're decorating this model
    private EventListenerList listenerList = new EventListenerList();

    public TableModelDecorator(TableModel model) {
        this.realModel = model;
        realModel.addTableModelListener(this);
    }

    // The following 9 methods are defined by the TableModel
    // interface; all of those methods forward to the real model.
    public void addTableModelListener(TableModelListener l) {
        listenerList.add(TableModelListener.class, l);
    }

    public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(TableModelListener.class, l);
    }

    public Class getColumnClass(int columnIndex) {
        return realModel.getColumnClass(columnIndex);
    }

    public int getColumnCount() {
        return realModel.getColumnCount();
    }

    public String getColumnName(int columnIndex) {
        return realModel.getColumnName(columnIndex);
    }

    public int getRowCount() {
        return realModel.getRowCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return realModel.getValueAt(rowIndex, columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return realModel.isCellEditable(rowIndex, columnIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        realModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    // The getRealModel method is used by subclasses to
    // access the real model.
    public TableModel getRealModel() {
        return realModel;
    }

    protected void fireTableChanged(TableModelEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }

    public void tableChanged(TableModelEvent e) {
        TableModelEvent newEvent =
                new TableModelEvent(
                        this,
                        e.getFirstRow(),
                        e.getLastRow(),
                        e.getColumn(),
                        e.getType());
        fireTableChanged(newEvent);
    }

}
