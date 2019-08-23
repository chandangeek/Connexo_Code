package com.energyict.mdc.engine.offline.gui.decorators;

import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.energyict.mdc.engine.offline.gui.actions.SortingSettingsAction;
import com.energyict.mdc.engine.offline.gui.actions.TableSortSupport;
import com.energyict.mdc.engine.offline.gui.models.AspectTableModel;
import com.energyict.mdc.engine.offline.gui.table.TableUtils;
import com.energyict.mdc.engine.offline.gui.table.renderer.SortableTableHeaderRenderer;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * @deprecated Use the TableRowSorter from now on
 */
@Deprecated
public class TableBubbleSortDecorator extends TableSortDecorator implements Comparator<Integer> {

    private static final MouseAdapter mouseAdapter = new MouseAdapter() {
    };
    private int indexes[];
    private boolean oldAscending = true;
    private boolean ascending = true;
    private int firstColumnSortedOn = -1;
    private int oldFirstColumnSortedOn = -1;
    private JTable theTable = null;
    private JPopupMenu popupMenu = new JPopupMenu();
    SortingSettingsAction sortSettingsAction;
    private SortSettings sortSettings = new SortSettings();
    private TableSortSupport tableSortSupport;
    // To remember the sort settings in case of a cancel:
    private List<String> names;
    private List<SortInfo> sortInfo;

    // The preferred way of calling functions is
    // First, addMouseListenerToHeaderInTable() afterwards sortInitially().
    // The next 4 variables are used to assure that a call of
    // sortInitially() BEFORE addMouseListenerToHeaderInTable()
    // also gives the expected result
    private boolean sortInitiallyCalled = false;
    private boolean addMouseListenerCalled = false;
    private int initialSortColumn = -1;
    private boolean initialAscending = true;
    private boolean sortStringsCaseIndependant = false;

    private PropertyChangeSupport propertyChangeSupport;
    public static String PROPERTY_ASCENDING = "ascending";
    public static String PROPERTY_SORTINGCOLUMN = "sortingColumn";

    protected class MyMouseAdapter extends MouseAdapter {

        // gde:
        // I choose to interpret a mousePressed followed by a mouseReleased
        // event as a mouseClicked event myself.
        // Reason: a mousePressed at point(x,y) followed by a mouseReleased
        //         at point (x',y') doesn't lead to a mouseClicked event
        //         unless x==x' and y==y'.
        //         I want a mouseClicked if the Press and the Release happens
        //         in the same column:
        // Rem.: we also turn off the column reordering
        private int iViewColumnPressed = -1;
        private TableBubbleSortDecorator sorter = null;

        public MyMouseAdapter(TableBubbleSortDecorator sorter) {
            this.sorter = sorter;
        }

        public void mousePressed(MouseEvent e) {
            // Pasquien 12/01/06: when clicking the table's header stop editing the table
            theTable.editingStopped(null);

            // Geert [2003-okt-02] react on the LEFT mouse button only:
            // if (e.getButton()==MouseEvent.BUTTON1) // Geert [2004-dec-13] RIGHT also
            iViewColumnPressed = theTable.getColumnModel().getColumnIndexAtX(e.getX());
        }

        public void mouseReleased(MouseEvent e) {
            int iViewColumnReleased = theTable.getColumnModel().getColumnIndexAtX(e.getX());
            if (iViewColumnReleased == -1) {
                iViewColumnPressed = -1;
                return;
            }
            if (iViewColumnReleased == iViewColumnPressed) {
                iViewColumnPressed = -1;
                int iModelColumn = theTable.convertColumnIndexToModel(iViewColumnReleased);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (getTableSortSupport() != null && !getTableSortSupport().isColumnSortable(iModelColumn)) {
                        return;
                    }
                    if (sortSettings.isSortedOn(iModelColumn)) {
                        sortSettings.toggleAscending(iModelColumn);
                        sort();
                    } else {
                        sorter.sort(iModelColumn);
                    } // Perform the sort
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                theTable.getTableHeader().repaint();
                theTable.repaint();
            }
        }

        public void resetListener() {
            iViewColumnPressed = -1;
        }
    }

    private MyMouseAdapter theMouseListener = null;
    private MouseMotionAdapter theMouseMotionListener = null;

    // The lone constructor must be passed a reference to a
    // TableModel. This class decorates that model with additional
    // sorting functionality.
    public TableBubbleSortDecorator(TableModel model) {
        super(model);
        allocate();
    }

    @Override
    // This model is a  TableModelListener on the real model
    public void tableChanged(TableModelEvent e) {
        Set<Integer> set = new HashSet<>();
        if (isActive() && (sortOnChange || isTableDataChangeEvent(e))) {
            allocate();
        }
        if (e.getType() != TableModelEvent.INSERT) {
            storeSelectedRows(set); // Geert [2005-sep-12]
        } else {
            // Pasquien: set the newly inserted rows as selected
            int row = e.getFirstRow();
            while (row <= e.getLastRow()) {
                set.add(row);
                row++;
            }
        }
        //  TableModelEvent e2 = e;
        boolean resorted = false;
        if (isActive() && sortOnChange) {
            resort();
            // e2 = getTableModelEvent(e);
            resorted = true;
        }
        super.tableChanged(getTableModelEvent(e));
        // modified 18/06/07 as a result of Mantis #2659
        // restoreSelectedRows after the super.fireTableChanged()
        // before => TheTable.getRowCount() gave 1, while TheTable.getSelectedRowCount() gave 2 ????
        // resulted in IndexOutOfBoundError...
        if (resorted) {
            restoreSelectedRows(set);
        }
    }

    protected TableModelEvent getTableModelEvent(TableModelEvent e) {

        //           e2 =  new TableModelEvent((TableModel) e.getSource(),
        //                                       (e.getFirstRow() == TableModelEvent.HEADER_ROW || e.getFirstRow()>=indexes.length ? e.getFirstRow(): indexes[e.getFirstRow()]),
        //                                       (e.getLastRow() == Integer.MAX_VALUE || e.getLastRow()>=indexes.length? e.getLastRow(): indexes[e.getLastRow()]),
        //                                       e.getColumn(),
        //                                       e.getType());

        // The e.getSource() will allways be the unsorted tablemodel ('this' is a tablemodelListener on the unsorted model)
        // We have to fire a tablechanged event (this for our JTable who's model is 'this' sorted model) : but
        // have to convert the row information of the unsorted model to row information of 'this' sorted model
        // This is done by getRow(realModelRow)
        int first = getRow(e.getFirstRow());
        int last;
        if (e.getFirstRow() == e.getLastRow()) {
            last = first;
        } else {
            last = getRow(e.getLastRow());
        }

        return new TableModelEvent((TableModel) e.getSource(), first, last, e.getColumn(), e.getType());
    }

    private boolean isTableDataChangeEvent(TableModelEvent e) {
        return (e.getType() == TableModelEvent.UPDATE && e.getLastRow() == Integer.MAX_VALUE);
    }

    public void resort() {
        if (isActive() && !sortSettings.getColumnsToSortOn().isEmpty()) {
            sort();
        }
    }

    @Override
    // 4 TableModel methods are overridden from
    // TableModelDecorator ...
    public Object getValueAt(int row, int column) {
        int row2Get = getRealModelRow(row);
        if (row2Get >= 0 && row2Get < getRealModel().getRowCount()) {
            return getRealModel().getValueAt(row2Get, column);
        }
        else {
            return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        boolean oldSortOnChange = this.getSortOnChange();
        int row2Set = getRealModelRow(row);
        if (row2Set >= 0 && row2Set < getRealModel().getRowCount()) {
            try {
                this.setSortOnChange(false);
                getRealModel().setValueAt(aValue, row2Set, column);
            } finally {
                this.setSortOnChange(oldSortOnChange);
            }
        }
    }

    public boolean isCellEditable(int row, int column) {
        int row2Test = getRealModelRow(row);
        if (row2Test >= 0 && row2Test < getRealModel().getRowCount()) {
            return getRealModel().isCellEditable(row2Test, column);
        }
        else {
            return false;
        }
    }

    // the AspectTableModel method getObjectAt() must be overridden
    public Object getObjectAt(int row) {
        int row2Get = getRealModelRow(row);
        if (row2Get >= 0 && row2Get < getRealModel().getRowCount()) {
            if (getRealModel() instanceof AspectTableModel) {
                return ((AspectTableModel) getRealModel()).getObjectAt(row2Get);
            }
        }
        return null;
    }

    public int getRealModelRow(int row) {
        if (!isActive()) {
            return row;
        }
        if (row < 0 || row >= indexes.length) {
            return row;
        }
        return indexes[row];
    }

    public int getRow(int realModelRow) {
        int result = -1;
        if (!isActive()) {
            return realModelRow;
        }
        if (realModelRow < 0 || realModelRow >= indexes.length) {
            return realModelRow;
        }
        for (int i = 0; i < indexes.length && result == -1; i++) {
            if (indexes[i] == realModelRow) {
                result = i;
            }
        }
        return result;
    }

    // -------------------------------------------------------------
    public boolean isSortingAscending() {
        return ascending;
    }

    public int getSortingColumn() {
        return firstColumnSortedOn;
    }

    // The following methods perform the bubble sort ...
    public void sort(int column) {
        if (!isActive()) {
            return;
        }
        List<Integer> cols = sortSettings.getColumnsToSortOn();
        if (!cols.isEmpty() && cols.get(0) == column) {
            boolean ascending = sortSettings.getAscending(column);
            sortSettings.clear();
            sortSettings.addIndexToSortOn(column, !ascending);
        } else {
            sortSettings.clear();
            sortSettings.addIndexToSortOn(column);
        }
        sort();
    }

    public void sort(int column, boolean asc) {
        if (!isActive()) {
            return;
        }
        sortSettings.clear();
        sortSettings.addIndexToSortOn(column, asc);
        sort();
    }

    public void sort() {
        if (!isActive()) {
            return;
        }

        Component glassPane = null;
        try {

            if (theTable != null && theTable.getTopLevelAncestor() != null) {
                Container container = theTable.getTopLevelAncestor();
                if (container instanceof RootPaneContainer) {
                    glassPane = ((RootPaneContainer) container).getGlassPane();
                    glassPane.addMouseListener(mouseAdapter);
                    glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    glassPane.setVisible(true);
                }
            }

            oldFirstColumnSortedOn = firstColumnSortedOn;
            firstColumnSortedOn = sortSettings.getColumnsToSortOn().size() > 0 ? sortSettings.getColumnsToSortOn().get(0) : -1;
            firePropertyChangeSortingColumn();
            if (firstColumnSortedOn != -1) {
                oldAscending = ascending;
                ascending = sortSettings.getAscending(firstColumnSortedOn);
                firePropertyChangeAscending();
            }
            int rowCount = getRowCount();

            // Remember the selected table items [gde (2003-sep-03)]
            Set<Integer> set = new HashSet<>();
            storeSelectedRows(set); // Geert [2005-sep-12] moved to separate method

            // Geert (2003-okt-21)
            // replaced the bubblesort by Java's quick sort
            List<Integer> lstTmp = new ArrayList<>(rowCount);
            for (int i = 0; i < rowCount; i++) {
                lstTmp.add(indexes[i]);
            }

            Collections.sort(lstTmp, this);

            for (int i = 0; i < rowCount; i++) {
                indexes[i] = lstTmp.get(i);
            }

            // Reselect the same table items [gde (2003-sep-03)]
            restoreSelectedRows(set); // Geert [2005-sep-12] moved to separate method

        } catch (Exception ex) {
            // various exceptions land here, usually due to clicking / key press while refreshing the panel
            // https://jira.eict.vpdc/browse/EISERVER-5396
            // https://jira.eict.vpdc/browse/EISERVERSG-4583
        } finally {

            if (glassPane != null) {
                glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                glassPane.removeMouseListener(mouseAdapter);
                glassPane.setVisible(false);
            }

        }
    }

    public void sortAndRefresh() {
        if (!isActive()) {
            return;
        }
        sort();
        theTable.getTableHeader().repaint();
        theTable.repaint();
    }

    public int compare(Integer iObj, Integer jObj) {
        int result = 0;
        int index = 0;
        Iterator<Integer> it = sortSettings.getColumnsToSortOn().iterator();
        while (result == 0 && it.hasNext()) {
            index = it.next();
            result = doCompare(iObj, jObj, index);
        }
        return getCompareResult(result, sortSettings.getAscending(index));
    }

    public int getCompareResult(int result) {
        return getCompareResult(result, ascending);
    }

    public int getCompareResult(int result, boolean ascending) {
        return ascending ? result : -result;
    }

    public void sortInitially(int column, boolean ascending) {
        if (!isActive()) {
            return;
        }
        // Make sure the header sorting icon is shown
        // when initially sorting the table programmatically
        setDefaultHeaderRenderer();
        if (addMouseListenerCalled) {
            sort(column, ascending);
        } else {
            initialSortColumn = column;
            initialAscending = ascending;
        }
        sortInitiallyCalled = true;
    }

    private int doCompare(int i, int j, int column) {
        TableModel realModel = getRealModel();
        Object io = realModel.getValueAt(i, column);
        Object jo = realModel.getValueAt(j, column);
        Class type = realModel.getColumnClass(column);
        return doCompare(io, jo, type);
    }

    protected int doCompare(Object io, Object jo, Class type) {
        // chech for nulls
        if (io == null && jo == null) {
            return 0;
        }
        if (io == null) {
            return -1; // Define null less than everything
        }
        if (jo == null) {
            return 1;
        }

        try {
            if (type == Level.class) {
                return compareLevels((Level) io, (Level) jo);
            } else if (type == String.class && sortStringsCaseIndependant) {
                return ((String)io).compareToIgnoreCase((String)jo);
            } else {
                Comparable ic = (Comparable) io;
                return ic.compareTo(jo);
            }
        } catch (ClassCastException ex) {
            /* column class does not implement comparable or was not Level after all
             * just compare the objects toString */
            return io.toString().toLowerCase().compareTo(jo.toString().toLowerCase());
        }
    }

    private int compareLevels (Level lvl1, Level lvl2) {
        int l1 = lvl1.intValue();
        int l2 = lvl2.intValue();
        if (l1 == l2) {
            return 0;
        } else {
            return l1 > l2 ? 1 : -1;
        }
    }

    private void allocate() {
        indexes = new int[getRowCount()];
        for (int i = 0; i < indexes.length; ++i) {
            indexes[i] = i;
        }
    }

    // -------------------------------------------------------------
    /*
    * This method picks good column sizes.
    * If all column heads are wider than the column's cells'
    * contents, then you can just use column.sizeWidthToFit().
    */
    public void initColumnSizes(JTable table) {
        TableUtils.initColumnSizes(table, 20);
    }

    protected void setTheTable(JTable table) {
        this.theTable = table;
    }

    // -------------------------------------------------------------
    /* Add a mouse listener to the Table to trigger a table sort
    * when a column heading is clicked in the JTable.
    */
    public void addMouseListenerToHeaderInTable(JTable table) {
        if (addMouseListenerCalled) {
            return;
        }

        theTable = table;
        theTable.setColumnSelectionAllowed(false);
        final JTableHeader th = theTable.getTableHeader();
        th.addMouseListener(theMouseListener = new MyMouseAdapter(this));

        // gde:
        // avoid that dragging column borders to change the column width
        // triggers a sort operation:
        theMouseMotionListener = new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (th.getResizingColumn() != null) {
                    theMouseListener.resetListener();
                }
            }
        };
        th.addMouseMotionListener(theMouseMotionListener);
        setDefaultHeaderRenderer();

        List<String> allColNames = new ArrayList<>();
        List<String> colNames = new ArrayList<>();
        for (int i = 0; i < theTable.getColumnModel().getColumnCount(); i++) {
            String colName = "";
            int viewIndex = theTable.convertColumnIndexToView(i);
            if (viewIndex >= 0) {
                TableColumn tc = theTable.getColumnModel().getColumn(viewIndex);
                //  Object ident = (tc==null) ? null : tc.getMessageIdentifier();  // the identifier is better used for the untranslated column name
                Object ident = (tc == null) ? null : tc.getHeaderValue();   // headervalue is what the user see - it's a translation of ...
                if (ident != null) {
                    colName = ident.toString();
                }

                allColNames.add(colName);
                if (this.getRealModel() instanceof TableSortSupport) {
                    if (((TableSortSupport) this.getRealModel()).isColumnSortable(i)) {
                        colNames.add(colName);
                    }
                } else {
                    colNames.add(colName); // If the model doesn't implement TableSortSupport, then we suppose all colNames are sortable
                }
            }
        }
        sortSettings.setAllColumnNames(allColNames);
        sortSettings.setColumnNames(colNames);

        addMouseListenerCalled = true;
        if (sortInitiallyCalled) // if called before this one, recall it:
        {
            sortInitially(initialSortColumn, initialAscending);
        }

        sortSettingsAction = new SortingSettingsAction(this);
        popupMenu.add(sortSettingsAction);
    }

    public void removeMouseListenersFromHeaderInTable() {
        addMouseListenerCalled = false;
        if (popupMenu.getComponentCount() >= 1) {
            JMenuItem menuItem = (JMenuItem) popupMenu.getComponent(0);
            if (menuItem != null && menuItem.getText().equals(sortSettingsAction.getValue(Action.NAME))) {
                popupMenu.remove(menuItem);
            }
        }
        if (theTable == null) {
            return;
        }
        JTableHeader th = theTable.getTableHeader();
        th.removeMouseListener(theMouseListener);
        th.removeMouseMotionListener(theMouseMotionListener);
    }

    protected void setDefaultHeaderRenderer() {
        if (theTable == null) {
            return;
        }
        TableColumnModel colModel = theTable.getColumnModel();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            if (getTableSortSupport() != null &&
                    !getTableSortSupport().isColumnSortable(i)) {
                continue;
            }
            colModel.getColumn(i).setHeaderRenderer(new SortableTableHeaderRenderer(theTable,this));
        }
    }

    // Pasquien 24/05/04 - give access to indexes for derived classes
    protected int[] getIndexes() {
        return indexes;
    }

    public SortSettings getSortSettings() {
        return sortSettings;
    }

    public void setSortSettings(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
    }

    public SortingSettingsAction addPopupMenuToTableHeader(JPopupMenu theMenu) {
        if (theMenu == null) {
            return null;
        }
        popupMenu = theMenu;
        if (theMenu.getComponentCount() > 0) {
            popupMenu.insert(new JPopupMenu.Separator(), 0);
        }
        sortSettingsAction = new SortingSettingsAction(this);
        popupMenu.insert(sortSettingsAction, 0);
        return sortSettingsAction;
    }

    public void rememberSortSettings() {
        names = sortSettings.getColumnNames();
        // It's important to make a new list, since sortSettings.getSortInfo() is
        // adapted according to the user's choice
        sortInfo = new ArrayList<>(sortSettings.getSortInfo());
    }

    public void rollbackToMarkedSortSettings() {
        // called when the user changes his mind and presses <Cancel>
        sortSettings = new SortSettings(names);
        for (SortInfo aSortInfo : sortInfo) {
            sortSettings.addToSort(aSortInfo);
        }
    }

    public TableSortSupport getTableSortSupport() {
        if (tableSortSupport == null) {
            try {
                tableSortSupport = (TableSortSupport) getRealModel();
            } catch (ClassCastException ex) {
                return null;
            }
        }
        return tableSortSupport;
    }

    public JMenuItem getSortSettingsMenu() {
        if (sortSettingsAction != null) {
            sortSettingsAction.setEnabled(isActive());
            return new JMenuItem(sortSettingsAction);
        }
        return null;
    }

    public void deleteSelectedRows() {
        if (!(getRealModel() instanceof AspectTableModel)) {
            return;
        }
        ListSelectionModel lsm = theTable.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        List<Integer> indices = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            if (!lsm.isSelectedIndex(i)) {
                continue;
            }
            indices.add(getRealModelRow(i));
        }
        Collections.sort(indices, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return -(o1.compareTo(o2));
            }
        });
        AspectTableModel realModel = (AspectTableModel) getRealModel();
        for (Integer indice : indices) {
            realModel.remove(indice);
        }
        theTable.clearSelection();
    }

    private void storeSelectedRows(Set<Integer> set) {
        int sel[];
        if (theTable != null) {
            sel = theTable.getSelectedRows();
            for (int i = 0; i < sel.length; i++) {
                if (sel[i] < indexes.length) {
                    set.add(indexes[sel[i]]);
                }
            }
        }
    }

    private void restoreSelectedRows(Set set) {
        // changed by kh to minimize calls to addRowSelectionInterval
        if (theTable != null && !set.isEmpty()) {
            theTable.clearSelection();
            int startIndex = -1;
            int lastIndex = -1;
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                if (set.contains(new Integer(indexes[i]))) {
                    if (startIndex == -1) {
                        startIndex = i;
                        lastIndex = i;
                    } else {
                        if (i == (lastIndex + 1)) {
                            lastIndex = i;
                        } else {
                            theTable.addRowSelectionInterval(startIndex, lastIndex);
                            startIndex = i;
                            lastIndex = i;
                        }
                    }
                }
            }
            if (startIndex >= 0) {
                theTable.addRowSelectionInterval(startIndex, lastIndex);
            }
        }
    }

    protected PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    protected void firePropertyChangeAscending() {
        getPropertyChangeSupport().firePropertyChange(PROPERTY_ASCENDING, oldAscending, ascending);
    }

    protected void firePropertyChangeSortingColumn() {
        getPropertyChangeSupport().firePropertyChange(PROPERTY_SORTINGCOLUMN, oldFirstColumnSortedOn, firstColumnSortedOn);
    }

    public void setSortStringsCaseIndependant(boolean caseIndependant) {
        this.sortStringsCaseIndependant = caseIndependant;
    }
}
