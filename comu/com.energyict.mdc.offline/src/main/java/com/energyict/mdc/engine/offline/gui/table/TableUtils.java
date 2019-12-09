package com.energyict.mdc.engine.offline.gui.table;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.actions.CopyPasteAdapter;
import com.energyict.mdc.engine.offline.gui.actions.RowSorterSortSettingsAction;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.table.renderer.*;
import com.energyict.mdc.engine.offline.model.DeviceMultiplier;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.SortableTableHeader;
import com.jidesoft.grid.SortableTableModel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class TableUtils {

    public static final String NORMAL = "NORMAL";
    public static final String FIT_WIDTH = "FIT_WIDTH";

    /**
     * Creates a new instance of TableUtils
     */
    private TableUtils() {
    }

    static public void customize(JTable table) {
        customize(table, true);
    }

    static public void customize(JTable table, boolean autoSizeColumns) {
        setHeaderRenderer(table);
        setRenderers(table);
        setEditors(table);
        if (autoSizeColumns) {
            initColumnSizes(table, 20);
        }
        new CopyPasteAdapter(table);
        int fontSize = table.getFont().getSize();
        if (fontSize > 13) {
            table.setRowHeight(fontSize + 4);
        } else {
            table.setRowHeight(16);
        }
        if (table.getRowSorter()!=null) {
            addSortSettingsPopupMenu(table);
        }
    }
    @SuppressWarnings("unchecked")
    static public void setHeaderRenderer(final JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            // RowSorter since java 1.6
            if (table.getRowSorter() != null){
                header.setDefaultRenderer(new SortableTableHeaderRenderer(table,(TableRowSorter) table.getRowSorter()));
            }else if (table instanceof SortableTable){
                if (((SortableTableModel) table.getModel()).isSortable()){
                    header.setDefaultRenderer(new SortableTableHeaderRenderer(table, (SortableTableModel) table.getModel()));
                    // remove the TableHeaderCellDecorator so sort order indexes are not displayed any more
                    ((SortableTableHeader) table.getTableHeader()).removeCellDecorator(((SortableTableHeader) table.getTableHeader()).getSortableTableHeaderCellDecorator());
                }else{
                    header.setDefaultRenderer(new HeaderRenderer(table));
                }
            }else{
                header.setDefaultRenderer(new HeaderRenderer(table));
            }
        }

    }

    static public void setRenderers(JTable table) {
        if (table.getDefaultRenderer(BigDecimal.class) == null) {
            table.setDefaultRenderer(BigDecimal.class, new BigDecimalRenderer());
        } //DJ [22/08/08] : added to prevent overwriting renderer with scale specified (channel reports!)
        table.setDefaultRenderer(Date.class, new DateRenderer());
        table.setDefaultRenderer(TimeZone.class, new TimeZoneRenderer());
        table.setDefaultRenderer(Level.class, new LevelRenderer());
        table.setDefaultRenderer(Quantity.class, new QuantityRenderer());
        table.setDefaultRenderer(TimeDuration.class, new TimeDurationTableCellRenderer());
        table.setDefaultRenderer(DeviceMultiplier.class, new DeviceMultiplierRenderer());
    }


    static public void setEditors(JTable table) {
        table.setDefaultEditor(BigDecimal.class, new BigDecimalCellEditor(table));
        table.setDefaultEditor(Integer.class, new IntegerCellEditor(table));
        table.setDefaultEditor(Date.class, new DateCellEditor());
        JTextField textField = new JTextField();
        textField.setFont(table.getFont());
        table.setDefaultEditor(String.class, new TextCellEditor(textField));
    }

    // to determine the best column widths
    // if maxRows>0: take into account the first maxRows rows
    // if maxRows<0: take into account the last -maxRows rows

    static public void initColumnSizes(JTable table, int maxRows) {
        TableModel model = table.getModel();
        TableColumnModel cModel = table.getColumnModel();
        int iRowCnt = table.getModel().getRowCount();
        if (maxRows > 0) {
            if (iRowCnt > maxRows) {
                iRowCnt = maxRows;
            }
        }
        Icon icon = SortableTableHeaderRenderer.getAscendingIcon();
        for (int i = 0; i < cModel.getColumnCount(); i++) {
            TableColumn column = cModel.getColumn(i);
            TableCellRenderer renderer = column.getHeaderRenderer();
            if (renderer == null && table.getTableHeader() != null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            if (renderer == null) {
                continue;
            }
            Component comp = renderer.getTableCellRendererComponent(
                    table,
                    column.getHeaderValue(),
                    false,
                    false,
                    0,
                    0);
            try {
                ((JLabel) comp).setIcon(icon);
            } catch (ClassCastException ex) {
            }
            ;
            int iMaxWidth = comp.getPreferredSize().width + 1;
            try {
                ((JLabel) comp).setIcon(null);
            } catch (ClassCastException ex) {
            }
            ;

            for (int iRow = (maxRows > 0) ? 0 : iRowCnt - 1;
                 ((maxRows > 0) ? (iRow < iRowCnt) : (iRow >= 0));
                /*iRow is in/decremented inside*/) {
                renderer = column.getCellRenderer();
                // Pasquien 20/12/05 - model.getValueAt(iRow, i) can cause a ClassCastException if columns
                // are not in the order as described in the tableModel
                // Rudi 06/02/2008 - also use the model column index when getting the cell renderer.
                int modelColumnIndex = table.convertColumnIndexToModel(i);
                if (renderer == null) {
                    renderer = table.getDefaultRenderer(model.getColumnClass(modelColumnIndex));
                }
                if (renderer == null) {
                    break;
                } // #4573
                // Renderers implementing the ColumnWidthCalculator
                // can calculate a suitable width for the column
                // they do not need to traverse the table (for a number of rows) to
                // get the biggest size.
                if (renderer instanceof ColumnWidthCalculator) {
                    iMaxWidth = Math.max(iMaxWidth, ((ColumnWidthCalculator) renderer).calcPreferredWidth());
                    break;
                }
                comp = renderer.getTableCellRendererComponent(
                        table,
                        model.getValueAt(iRow, modelColumnIndex),
                        false,
                        false,
                        iRow,
                        i);
                if (comp == null) {
                    break;
                } // for safety

                int iCellWidth = comp.getPreferredSize().width + 1;
                iMaxWidth = Math.max(iMaxWidth, iCellWidth);
                if (maxRows > 0) {
                    iRow++;
                } else {
                    iRow--;
                }
            }
            column.setPreferredWidth(iMaxWidth);
        }
    }

    static public int getWidth(JTable table, int columnIndex, Object value) {
        TableColumnModel cModel = table.getColumnModel();
        TableColumn column = cModel.getColumn(columnIndex);
        TableCellRenderer renderer = column.getCellRenderer();
        if (renderer == null) {
            renderer = table.getDefaultRenderer(table.getModel().getColumnClass(columnIndex));
        }
        Component comp = renderer.getTableCellRendererComponent(
                table,
                value,
                false,
                false,
                0,
                columnIndex);
        return comp.getPreferredSize().width + 1;
    }

    static public void copyTableData(JTable table) {
        int[] rows = new int[table.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        copyTableData(table, rows);
    }

    static public void copySelectedTableData(JTable table) {
        copyTableData(table, table.getSelectedRows());
    }

    static public void copyTableData(JTable table, int[] rowsselected) {
        TableColumnModel cModel = table.getColumnModel();
        TableModel model = table.getModel();
        StringBuffer sbf = new StringBuffer();
        int numcols = table.getColumnCount();
        int numrows = rowsselected.length;

        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                int jm = table.convertColumnIndexToModel(j);
                TableColumn column = cModel.getColumn(j);
                TableCellRenderer renderer = column.getCellRenderer();
                if (renderer == null) {
                    renderer = table.getDefaultRenderer(model.getColumnClass(jm));
                }
                Component comp = renderer.getTableCellRendererComponent(
                        table, model.getValueAt(table.convertRowIndexToModel(rowsselected[i]), jm),
                        false, false, rowsselected[i], j);

                if (comp instanceof JLabel) {
                    String text = ((JLabel) comp).getText();
                    if (text == null) {
                        text = "";
                    } else if (text.trim().length() == 0) {
                        //fallback for JLabels which only show icon and have the value in their tooltip
                        text = ((JLabel) comp).getToolTipText();
                        if(text == null) {
                            text = "";
                        }
                    }
                    sbf.append(text);
                } else if (comp instanceof JCheckBox) {
                    sbf.append(TranslatorProvider.instance.get().getTranslator().getTranslation(
                            ((JCheckBox) comp).isSelected() ? "yes" : "no"));
                } else {
                    if (renderer instanceof Transferable) {
                        if (((Transferable) renderer).isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            try {
                                sbf.append((String) ((Transferable) renderer).getTransferData(DataFlavor.stringFlavor));
                            } catch (UnsupportedFlavorException exc) {
                                // cannot happen - see test
                            } catch (IOException exc) {
                                // do nothing
                            }
                        }
                    } else {
                        if (comp instanceof Transferable) {
                            if (((Transferable) comp).isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                try {
                                    sbf.append((String) ((Transferable) comp).getTransferData(DataFlavor.stringFlavor));
                                } catch (UnsupportedFlavorException exc) {
                                    // cannot happen - see test
                                } catch (IOException exc) {
                                    // do nothing
                                }
                            }
                        }
                    }
                }
                if (j < numcols - 1) {
                    sbf.append("\t");
                }
            }
            sbf.append("\n");
        }
        StringSelection stsel = new StringSelection(sbf.toString());
        Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }

    static public boolean canPrint() {
        try {
            return JTable.class.getMethod("print", (Class[]) null) != null;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    static public void print(JTable table) {
        try {
            Method method = JTable.class.getMethod("print", (Class[]) null);
            method.invoke(table, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new ApplicationException(ex);
        }
    }

    static private Object getPrintMode(String mode) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class printModeClass = Class.forName("javax.swing.JTable$PrintMode");
        Method method = printModeClass.getMethod("valueOf", new Class[]{String.class});
        return method.invoke(null, mode);
    }

    static public void print(JTable table, String mode) {
        try {
            Method method = JTable.class.getMethod("print", new Class[]{Class.forName("javax.swing.JTable$PrintMode")});
            method.invoke(table, getPrintMode(mode));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException  ex) {
            throw new ApplicationException(ex);
        }
    }

    static public void print(JTable table, String mode, MessageFormat headerFormat, MessageFormat footerFormat) {
        try {
            Method method = JTable.class.getMethod(
                    "print",
                    new Class[]{
                            Class.forName("javax.swing.JTable$PrintMode"),
                            MessageFormat.class,
                            MessageFormat.class});
            method.invoke(table, getPrintMode(mode), headerFormat, footerFormat);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException  ex) {
            throw new ApplicationException(ex);
        }
    }

    static public TablePnlState saveState(JTable tabletoSave, TableBubbleSortDecorator sortedModeltoSave) {
        TablePnlState state = new TablePnlState();
        state.setSortSettings(sortedModeltoSave.getSortSettings());
        state.setAscending(sortedModeltoSave.isSortingAscending());
        state.setSortingColumn(sortedModeltoSave.getSortingColumn());
        for (int i = 0; i < tabletoSave.getSelectedRowCount(); i++) {
            state.addRow(tabletoSave.getSelectedRows()[i]);
        }
        return state;
    }

    static public void applyState(TablePnlState state, JTable tableToRestore, TableBubbleSortDecorator sortedModelToRestore) {
        if (state == null) {
            return;
        }
        SortSettings sortSettings = state.getSortSettings();
        if (sortSettings != null) {
            sortedModelToRestore.setSortSettings(sortSettings);
            sortedModelToRestore.sortAndRefresh();
        }
        ListSelectionModel lsm = tableToRestore.getSelectionModel();
        lsm.clearSelection();
        for (Integer row : state.getSelectedRows()) {
            lsm.addSelectionInterval(row, row);
        }
    }

    static private void addSortSettingsPopupMenu(final JTable table) {
        if (table==null || table.getRowSorter()==null) return;
        for (MouseListener listener : table.getTableHeader().getMouseListeners()) {
            if (listener instanceof TableUtilsMouseAdapter) {
                return; // TableUtilsMouseAdapter already set previously
            }
        }
        TableUtilsMouseAdapter mouseAdapter = new TableUtilsMouseAdapter(table);
        table.getTableHeader().addMouseListener(mouseAdapter);
    }

    static public JPopupMenu getTableHeaderPopupMenu(final JTable table) {
        if (table==null) return null;
        for (MouseListener listener : table.getTableHeader().getMouseListeners()) {
            if (listener instanceof TableUtilsMouseAdapter) {
                return ((TableUtilsMouseAdapter)listener).getPopupMenu();
            }
        }
        // TableUtilsMouseAdapter not yet added, so do it now:
        TableUtilsMouseAdapter mouseAdapter = new TableUtilsMouseAdapter(table);
        table.getTableHeader().addMouseListener(mouseAdapter);
        return mouseAdapter.getPopupMenu();
    }

    static public void addTableHeaderPopupMenuAction(final JTable table, final AbstractAction menuAction) {
        addTableHeaderPopupMenuItem(table, new JMenuItem(menuAction));
    }

    static public void addTableHeaderPopupMenuItem(final JTable table, final JMenuItem menuItem) {
        if (table==null || table.getRowSorter()==null) return;
        for (MouseListener listener : table.getTableHeader().getMouseListeners()) {
            if (listener instanceof TableUtilsMouseAdapter) {
                ((TableUtilsMouseAdapter)listener).addMenuComponent(menuItem);
                return;
            }
        }
        // TableUtilsMouseAdapter not yet added, so do it now:
        TableUtilsMouseAdapter mouseAdapter = new TableUtilsMouseAdapter(table);
        table.getTableHeader().addMouseListener(mouseAdapter);
        mouseAdapter.addMenuComponent(menuItem);
    }

    static public TableRowSorter createSortableTable(JTable table, TableModel tableModel) {
        table.setModel(tableModel);
        TableRowSorter tableRowSorter = new TableRowSorter<>(tableModel);
        tableRowSorter.setMaxSortKeys(1);
        table.setRowSorter(tableRowSorter);
        customize(table);
        return tableRowSorter;
    }

    static public class TableUtilsMouseAdapter extends MouseAdapter {
        private List<Component> menuComponents = new ArrayList();
        private final JTable table;

        public TableUtilsMouseAdapter(final JTable table) {
            this.table = table;
        }

        public void addMenuComponent(Component menuComponent) {
            menuComponents.add(menuComponent);
        }

        public JPopupMenu getPopupMenu() {
            JPopupMenu popupMenu = new JPopupMenu();
            if (table.getRowSorter()!=null) {
                popupMenu.add(new RowSorterSortSettingsAction(table));
            }
            for (Component each : menuComponents) {
                popupMenu.add(each);
            }
            return popupMenu;
        }

        @Override
        public void mousePressed(MouseEvent evt) { showPopup(evt); }
        @Override
        public void mouseReleased(MouseEvent evt) { showPopup(evt); }

        private void showPopup(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
            }
            // Left-clicking in the header = sorting on that column only again
            // in case it was on multiple columns
            if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount()==1) {
                RowSorter rowSorter = table.getRowSorter();
                if (rowSorter!=null &&
                    rowSorter instanceof TableRowSorter &&
                    ((TableRowSorter)rowSorter).getMaxSortKeys()!=1) {
                    ((TableRowSorter)rowSorter).setMaxSortKeys(1);
                }
            }
        }
    }
}
