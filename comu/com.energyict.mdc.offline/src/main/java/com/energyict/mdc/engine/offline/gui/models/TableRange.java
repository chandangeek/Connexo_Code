/*
 * TableRange.java
 *
 * Created on 30 maart 2005, 16:10
 */

package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author pasquien
 */
public class TableRange extends AbstractTableModel
        implements ListSelectionListener {

    protected JTable dataTable;
    protected int startRow = -1;
    protected int endRow = -1;
    protected int startColumn;
    protected int endColumn;

    private PropertyChangeSupport propertyChangeSupport;

    public TableRange(JTable dataTable) {
        this.dataTable = dataTable;
        dataTable.getSelectionModel().addListSelectionListener(this);
        dataTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
    }

    public void setRange(int startRow, int startColumn, int endRow, int endColumn) {
        if (startRow > endRow || startColumn > endColumn) {
            throw new IllegalArgumentException("startRow > endRow or startColum > endColumn");
        }

        dataTable.getSelectionModel().removeListSelectionListener(this);
        dataTable.getColumnModel().getSelectionModel().removeListSelectionListener(this);
        dataTable.clearSelection();

        setStartRow(startRow);
        setStartColumn(startColumn);
        setEndRow(endRow);
        setEndColumn(endColumn);
        firePropertyChange();

        dataTable.getSelectionModel().addSelectionInterval(getStartRow(), getEndRow());
        dataTable.getColumnModel().getSelectionModel().addSelectionInterval(getStartColumn(), getEndColumn());
        ;

        dataTable.getSelectionModel().addListSelectionListener(this);
        dataTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
    }

    public void emptyRange() {
        this.setRange(-1, 0, -1, 0);
    }

    public boolean isEmptyRange() {
        return (startRow == -1 && endRow == -1);
    }

    public void fullRange() {
        this.setRange(0, 0, dataTable.getRowCount() - 1, dataTable.getColumnCount() - 1);
    }

    public boolean isFullRange() {
        return (startRow == 0 && startColumn == 0 &&
                endRow == dataTable.getRowCount() - 1 && endColumn == dataTable.getRowCount() - 1);
    }

    public JTable getDataTable() {
        return dataTable;
    }

    public int getStartRow() {
        return startRow;
    }

    private void setStartRow(int startRow) throws IllegalArgumentException {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    private void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getStartColumn() {
        return startColumn;
    }

    private void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    private void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    private PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    public int getRealColumn(int column) {
        return column + startColumn;
    }

    public int getRealRow(int row) {
        return row + startRow;
    }

    public Class getColumnClass(int col) {
        TableModel realModel = dataTable.getModel();
        return realModel.getColumnClass(getRealColumn(col));
    }

    public int getColumnCount() {
        return endColumn - startColumn + 1;
    }

    public String getColumnName(int col) {
        TableModel realModel = dataTable.getModel();
        return realModel.getColumnName(getRealColumn(col));
    }

    public int getColumnIndex(String name) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (getColumnName(i).equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public int getRowCount() {
        return endRow - startRow + 1;
    }

    public Object getValueAt(int row, int col) {
        TableModel realModel = dataTable.getModel();
        return realModel.getValueAt(getRealRow(row), getRealColumn(col));
    }

    public boolean isRowInRange(int realRow) {
        return (realRow >= startRow &&
                realRow <= endRow);
    }

    public boolean isColumnInRange(int realCol) {
        return (realCol >= startColumn &&
                realCol <= endColumn);
    }

    public boolean isInRange(int realRow, int realCol) {
        return isRowInRange(realRow) && isColumnInRange(realCol);
    }

    public boolean isSingleCell() {
        return (startRow == endRow && startColumn == endColumn);
    }

    public boolean isSingleRow() {
        return (startRow == endRow);
    }

    public boolean isSingleColumn() {
        return (startColumn == endColumn);
    }

    public String toString() {
        return "(" + startRow + "," + startColumn + ") - (" + endRow + "," + endColumn + ")";
    }

    public void copyData() {
        TableColumnModel cModel = dataTable.getColumnModel();
        TableModel model = dataTable.getModel();

        StringBuffer sbf = new StringBuffer();
        int numcols = getColumnCount();
        int numrows = getRowCount();

        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                int jm = dataTable.convertColumnIndexToModel(getRealColumn(j));
                TableColumn column = cModel.getColumn(jm);
                TableCellRenderer renderer = column.getCellRenderer();
                if (renderer == null) {
                    renderer = dataTable.getDefaultRenderer(getColumnClass(jm));
                }
                Component comp = renderer.getTableCellRendererComponent(
                        dataTable, model.getValueAt(getRealRow(i), jm), false, false, getRealRow(i), jm);

                if (comp instanceof JLabel) {
                    String text = ((JLabel) comp).getText();
                    sbf.append(text);
                } else if (comp instanceof JCheckBox) {
                    sbf.append(TranslatorProvider.instance.get().getTranslator().getTranslation(
                            ((JCheckBox) comp).isSelected() ? "yes" : "no"));
                }
                if (j < numcols - 1) {
                    sbf.append("\t");
                }
            }
            sbf.append("\n");
        }
        StringSelection stsel = new StringSelection(sbf.toString());
        Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(stsel, stsel);
    }

    public String[] getColumnNames() {
        String[] columnNames = new String[getColumnCount()];
        for (int col = 0; col < columnNames.length; col++) {
            columnNames[col] = getColumnName(col);
        }
        return columnNames;
    }


    public void valueChanged(ListSelectionEvent e) {

        // rows
        if (e.getSource() == dataTable.getSelectionModel()) {
            setStartRow(dataTable.getSelectionModel().getMinSelectionIndex());
            setEndRow(dataTable.getSelectionModel().getMaxSelectionIndex());
            firePropertyChange();
        }
        // columns
        if (e.getSource() == dataTable.getColumnModel().getSelectionModel()) {
            setStartColumn(dataTable.getColumnModel().getSelectionModel().getMinSelectionIndex());
            setEndColumn(dataTable.getColumnModel().getSelectionModel().getMaxSelectionIndex());
            firePropertyChange();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    protected void firePropertyChange() {
        getPropertyChangeSupport().firePropertyChange(null, null, null);
    }

}
