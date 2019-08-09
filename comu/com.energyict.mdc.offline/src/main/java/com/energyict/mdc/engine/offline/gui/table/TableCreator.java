/*
 * CustomTableModel.java
 *
 * Created on 25 september 2003, 11:26
 */

package com.energyict.mdc.engine.offline.gui.table;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.Helpers;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.rows.AbstractRowData;
import com.energyict.mdc.engine.offline.gui.table.editor.JButtonCellEditor;
import com.energyict.mdc.engine.offline.gui.table.editor.JCheckBoxCellEditor;
import com.energyict.mdc.engine.offline.gui.table.editor.JComboBoxCellEditor;
import com.energyict.mdc.engine.offline.gui.table.editor.JTextAreaCellEditor;
import com.energyict.mdc.engine.offline.gui.table.renderer.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

/**
 * ColumnWidthObjects can be set as =n,>n,<n, null or a "val..."
 *
 * @author Koen
 *         Changes:
 *         KV 04062004 if no resource exist, use another column header descriptor...
 */
@SuppressWarnings("serial")
public class TableCreator extends AbstractTableModel {

    private static final Log logger = LogFactory.getLog(TableCreator.class);

    private JTable jTable;
    private Class cls;
    private AbstractRowData abstractRowData;
    private java.util.List rows;

    /*
      * Construct a TableCreator
      * @param autoResize Allow swing to adapt columnwidths to the width of the panel. Default = true
      * @param parent needed when we want autoresize false and horizontal scrollbar instead. E.g. new TableCreator(class.xx,false,this)
      */

    public TableCreator(Class cls) {
        this(cls, true, null, true);
    }

    public TableCreator(Class cls, boolean autoResize) {
        this(cls, autoResize, null, true);
    }

    public TableCreator(Class cls, Component parent) {
        this(cls, true, parent, true);
    }


    public TableCreator(Class cls, boolean autoResize, Component parent) {
        this(cls, autoResize, parent, true);
    }

    public TableCreator(Class cls, boolean autoResize, Component parent, boolean sort) {
        this.rows = new ArrayList();
        this.cls = cls;
        this.jTable = new JTable();
        if (!autoResize) {
            this.jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        try {
            this.abstractRowData = (AbstractRowData) cls.newInstance();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, constructor, Error instantiating " + cls.getName() + ", probably no default constructor defined, exit program");
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, constructor, Error instantiating " + cls.getName() + ", IllegalAccessException, exit program");
        }
        if (sort) {
            RestrictedTableBubbleSortDecorator restrictedTableSortDecorator = new RestrictedTableBubbleSortDecorator(this);
            this.jTable.setModel(restrictedTableSortDecorator);
            restrictedTableSortDecorator.addMouseListenerToHeaderInTable(this.jTable);
        } else {
            this.jTable.setModel(this);
        }
        SetColumnWidths();
        setEventHandler();
    }

    public JTable getJTable() {
        return this.jTable;
    }

    public void setRows(java.util.List rows) {
        this.rows = rows;
        if (rows != null) {
            fireTableDataChanged();
        }
    }

    public java.util.List getRows() {
        return this.rows;
    }

    public void SetColumnWidths() {
        for (int i = 0; i < getColumnCount(); i++) {
            setRenderer(i);
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col >= getColumnCount()) {
            return "";
        } else {
            // KV 04062004
            String translationKey = this.abstractRowData.getColumnTranslationKeys()[col];
            return TranslatorProvider.instance.get().getTranslator().getTranslation(translationKey, false);
        }
    }

    public int getRowCount() {
        return this.rows.size();
    }

    public int getColumnCount() {
        return this.abstractRowData.getColumnCount();
    }

    public void setRowHeight(int height) {
        getJTable().setRowHeight(height);
    }

    public Object getValueAt(int row, int col) {

        if (col >= getColumnCount()) {
            throw new RuntimeException("TableCreator, getValueAt, col index (" + col + ") >= abstractRowData.getColumnCount() (" + getColumnCount() + ")!, cannot continue");
        }
        if (row >= this.rows.size()) {
            throw new RuntimeException("TableCreator, getValueAt, row index (" + row + ") >= rows.size() (" + this.rows.size() + ")!, cannot continue");
        }
        Object rowObject = this.rows.get(row);

        if (!this.cls.isAssignableFrom(this.rows.get(row).getClass())) {
            throw new RuntimeException("TableCreator, getValueAt, Classes not equal!");
        }
        try {
            return rowObject.getClass().getMethod(this.abstractRowData.getColumnGetters()[col], null).invoke(rowObject, null);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, getValueAt, invalid method name in row class, " + this.abstractRowData.getColumnGetters()[col] + " does not exist");
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, getValueAt, IllegalAccessException, " + this.abstractRowData.getColumnGetters()[col]);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            logger.error(e.getTargetException().getMessage(), e.getTargetException());
            throw new RuntimeException("TableCreator, getValueAt, InvocationTargetException, " + this.abstractRowData.getColumnGetters()[col]);
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return Helpers.translateClass(this.abstractRowData.getColumnClass(c));
    }


    @Override
    public boolean isCellEditable(int row, int col) {
        if (col >= getColumnCount()) {
            throw new RuntimeException("TableCreator, isCellEditable, col index (" + col + ") >= abstractRowData.getColumnCount() (" + getColumnCount() + ")!, cannot continue");
        }
        return (this.abstractRowData.getColumnSetters()[col] != null);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col >= getColumnCount()) {
            throw new RuntimeException("TableCreator, setValueAt, col index (" + col + ") >= abstractRowData.getColumnCount() (" + getColumnCount() + ")!, cannot continue");
        }
        if (row >= this.rows.size()) {
            throw new RuntimeException("TableCreator, setValueAt, row index (" + row + ") >= rows.size() (" + this.rows.size() + ")!, cannot continue");
        }
        Object rowObject = this.rows.get(row);
        if (!this.cls.isAssignableFrom(this.rows.get(row).getClass())) {
            throw new RuntimeException("TableCreator, setValueAt, Classes not equal!");
        }
        try {

            if (this.abstractRowData.getColumnSetters()[col] != null) {
                int rowCount = getRowCount();
                rowObject.getClass().getMethod(this.abstractRowData.getColumnSetters()[col], new Class[]{this.abstractRowData.getColumnClass(col)}).invoke(rowObject, value);

                if (rowCount != getRowCount()) {// if nr of rows changed, update whole table!
                    fireTableDataChanged();
                } else {
                    fireTableRowsUpdated(row, row);
                }
            }
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, getValueAt, invalid method name in row class, " + this.abstractRowData.getColumnGetters()[col] + " does not exist", e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("TableCreator, getValueAt, IllegalAccessException, " + this.abstractRowData.getColumnGetters()[col], e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            logger.error(e.getTargetException().getMessage(), e.getTargetException());
            throw new ApplicationException("Error while invoking " + this.abstractRowData.getColumnGetters()[col] + ": " + e.getTargetException().getMessage(), e.getTargetException());
        }
        if (col >= getColumnCount()) {
            throw new RuntimeException("TableCreator, setValueAt, col index (" + col + ") >= abstractRowData.getColumnCount() (" + getColumnCount() + ")!, cannot continue");
        }

    }

    private void setRenderer(int col) {
        // cell renderers
        JButtonRenderer br;
        JComboBoxRenderer cbr;
        JCheckBoxRenderer cr;
        DateRenderer dr;
        JTextAreaRenderer tar;

        // cell editors
        JCheckBoxCellEditor ce;
        JButtonCellEditor be;
        JComboBoxCellEditor cbe;
        JTextAreaCellEditor tae;

        TableColumn column = getJTable().getColumnModel().getColumn(col);
        Object format = this.abstractRowData.getColumnWidthObject(col);

        if (this.abstractRowData.getColumnClass(col).isAssignableFrom(JComboBox.class)) {
            cbr = new JComboBoxRenderer();
            column.setCellRenderer(cbr);
            cbe = new JComboBoxCellEditor(new JComboBox());
            column.setCellEditor(cbe);
            doSetHeaderWidth(col, this.abstractRowData.getColumnWidthObject(col));
        } else if (this.abstractRowData.getColumnClass(col).isAssignableFrom(JTextArea.class)) {
            tar = new JTextAreaRenderer();
            column.setCellRenderer(tar);
            tae = new JTextAreaCellEditor(new JTextArea());
            column.setCellEditor(tae);
            doSetHeaderWidth(col, this.abstractRowData.getColumnWidthObject(col));
        } else if (this.abstractRowData.getColumnClass(col).isAssignableFrom(JButton.class)) {
            br = new JButtonRenderer();
            column.setCellRenderer(br);
            be = new JButtonCellEditor(new JCheckBox(""));
            column.setCellEditor(be);
            doSetHeaderWidth(col, this.abstractRowData.getColumnWidthObject(col));
        } else if (this.abstractRowData.getColumnClass(col).isAssignableFrom(JCheckBox.class)) {
            cr = new JCheckBoxRenderer();
            column.setCellRenderer(cr);
            ce = new JCheckBoxCellEditor(new JCheckBox(""));
            column.setCellEditor(ce);
        } else if (this.abstractRowData.getColumnClass(col).isAssignableFrom(Date.class)) {
            dr = new DateRenderer();
            column.setCellRenderer(dr);
            if (format == null) {
                format = new Date();
            }
        }

        doSetHeaderWidth(col, format);

    } // private void setRenderer(int col)

    private void doSetHeaderWidth(int col, Object format) {
        TableColumn column = getJTable().getColumnModel().getColumn(col);
        int cellWidth = -1;
        String formatstr = null;

        if ((format != null) && (format.getClass().isAssignableFrom(String.class))) {
            formatstr = (String) format;
        }

        if (formatstr != null) {
            try {
                if (formatstr.indexOf("=") != -1) {
                    // get columnwidth...
                    cellWidth = Integer.parseInt(formatstr.substring(formatstr.indexOf("=") + 1));
                    column.setMaxWidth(cellWidth);
                    column.setMinWidth(cellWidth);
                    format = null;
                } else if (formatstr.indexOf("<") != -1) {
                    // get columnwidth...
                    cellWidth = Integer.parseInt(formatstr.substring(formatstr.indexOf("<") + 1));
                    column.setMaxWidth(cellWidth);
                    format = null;
                } else if (formatstr.indexOf(">") != -1) {
                    // get columnwidth...
                    cellWidth = Integer.parseInt(formatstr.substring(formatstr.indexOf(">") + 1));
                    column.setMinWidth(cellWidth);
                    format = null;
                }
            } catch (NumberFormatException e) {
                throw new ApplicationException("TableCreator, error in format expression (" + formatstr + ")", e);
            }
        }

        Component comp = getJTable().getDefaultRenderer(getColumnClass(col)).getTableCellRendererComponent(getJTable(), format, false, false, 0, col);

        if (cellWidth == -1) {
            cellWidth = comp.getPreferredSize().width;
        }

        column.setPreferredWidth(cellWidth);
    }

    private void setEventHandler() {
        getJTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                theTableMouseReleased(evt);
            }
        });
    }

    private void theTableMouseReleased(java.awt.event.MouseEvent evt) {
        if (getJTable().getSelectedColumn() == -1) {
            return;
        }
        Class colCls = getJTable().getColumnClass(getJTable().getSelectedColumn());
        TableColumn column = getJTable().getColumnModel().getColumn(getJTable().getSelectedColumn());
        if (colCls.isAssignableFrom(JButton.class)) {
            ((JButtonCellEditor) column.getCellEditor()).fireEditingStopped();
        }
        if (colCls.isAssignableFrom(JCheckBox.class)) {
            ((JCheckBoxCellEditor) column.getCellEditor()).fireEditingStopped();
        }
        if (colCls.isAssignableFrom(JComboBox.class)) {
            ((JComboBoxCellEditor) column.getCellEditor()).fireEditingStopped();
        }
    }
}