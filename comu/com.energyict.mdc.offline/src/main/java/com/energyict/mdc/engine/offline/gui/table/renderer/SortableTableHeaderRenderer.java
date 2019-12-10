package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;
import com.jidesoft.grid.SortableTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class SortableTableHeaderRenderer extends HeaderRenderer  {

    private TableBubbleSortDecorator sorter;
    // Since java 1.6
    private TableRowSorter<? extends TableModel> rowSorter;
    // for use with Jide SortableTable
    private SortableTableModel sortableModel;


    private static Icon ascendingIcon;
    private static Icon descendingIcon;
    private static Icon ascendingDIcon;
    private static Icon descendingDIcon;

    static {
        ascendingIcon = new ImageIcon(SortableTableHeaderRenderer.class.getResource("/images/up.gif"));
        descendingIcon = new ImageIcon(SortableTableHeaderRenderer.class.getResource("/images/down.gif"));
        ascendingDIcon = new ImageIcon(SortableTableHeaderRenderer.class.getResource("/images/up_disabled.gif"));
        descendingDIcon = new ImageIcon(SortableTableHeaderRenderer.class.getResource("/images/down_disabled.gif"));
    }

    public static Icon getAscendingIcon() {
        return ascendingIcon;
    }

    /**
     * Creates a new instance of SortableTableCellRenderer
     * @param table Jtable the renderer will be used
     * @param sorter responsible for sorting and keeping the sort orders
     */
    public SortableTableHeaderRenderer(JTable table, TableBubbleSortDecorator sorter) {
        super(table);
        this.sorter = sorter;
    }

    public SortableTableHeaderRenderer(JTable table, TableRowSorter<? extends TableModel> rowSorter) {
        super(table);
        this.rowSorter = rowSorter;
    }

    public SortableTableHeaderRenderer(JTable table, SortableTableModel model) {
        super(table);
        this.sortableModel = model;
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        // Add the sort icon to the label
        if (rowSorter != null){
            label.setIcon(getIconFromRowSorter(table.convertColumnIndexToModel(column)));
        }else if (sortableModel != null) {
            label.setIcon(getIconFromSortableTableModel(sortableModel, column));
        }else {
            label.setIcon(getIconFromSorter(table, column));
        }
        return label;
    }

    private Icon getIconFromSorter(JTable table, int column) {
        if (sorter == null){
            return null;
        }
        List<Integer> cols = sorter.getSortSettings().getColumnsToSortOn();
        int columnInModel = table.convertColumnIndexToModel(column);
        int sortOrder = cols.indexOf(columnInModel);
        if (sortOrder >=0){
            int iconOrder;
            if (cols.size() == 1){
                iconOrder = SortOrderIcon.SORTORDER_UNDEFINED;   // Do not mention
            }else{
                iconOrder = sortOrder + 1; // 1 based count
            }
            return new SortOrderIcon(sorter.getSortSettings().getSortInfo().get(sortOrder), iconOrder);
        } else {
            return null;
        }
    }

    private Icon getIconFromSortableTableModel(SortableTableModel model, int column) {
        if (model.isColumnSortable(column) && model.isColumnSorted(column)){
            if (model.isColumnAscending(column)){
                return new SortOrderIcon(new SortInfo(null,SortOrder.ASCENDING),(model.getSortingColumns().size()==1 ? SortOrderIcon.SORTORDER_UNDEFINED : model.getColumnSortRank(column)+1));
            }else{
                return new SortOrderIcon(new SortInfo(null,SortOrder.DESCENDING),(model.getSortingColumns().size()==1 ? SortOrderIcon.SORTORDER_UNDEFINED : model.getColumnSortRank(column)+1));
            }
        }
        return null;
    }

    private Icon getIconFromRowSorter( int column) {
        List<? extends RowSorter.SortKey> sortKeys = rowSorter.getSortKeys();
        for (RowSorter.SortKey sortKey : sortKeys){
          if (sortKey.getColumn()==column && rowSorter.isSortable(column)){
              SortOrder order = sortKey.getSortOrder();
              switch(order){
                  case UNSORTED: return null;
                  case ASCENDING: return new SortOrderIcon(new SortInfo(null,SortOrder.ASCENDING),(rowSorter.getSortKeys().size()==1 ? SortOrderIcon.SORTORDER_UNDEFINED : sortKeys.indexOf(sortKey)+1));
                  case DESCENDING: return new SortOrderIcon(new SortInfo(null,SortOrder.DESCENDING),(rowSorter.getSortKeys().size()==1 ? SortOrderIcon.SORTORDER_UNDEFINED : sortKeys.indexOf(sortKey)+1));
              }
          }
        }
        return null;
    }

    private class SortOrderIcon implements Icon{

        final static int SORTORDER_UNDEFINED = -1;

        private SortInfo sortInfo;
        private int sortOrderIndex = SORTORDER_UNDEFINED;
        private Icon undecoratedIcon = null;
        private Icon disabledUndecoratedIcon = null;

        SortOrderIcon(SortInfo sortInfo, int sortOrderIndex){
           this.sortInfo = sortInfo;
           this.sortOrderIndex = sortOrderIndex;
           initUndecoratedIcons();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c.isEnabled()){
                undecoratedIcon.paintIcon(c, g, x, y);
            }else{
                disabledUndecoratedIcon.paintIcon(c, g, x, y);
            }
            if (sortOrderIndex != SORTORDER_UNDEFINED){
                Graphics g2 = g.create();
                g2.setFont(UIManager.getFont("TableHeader.font").deriveFont(10f));
                FontMetrics metrics = g.getFontMetrics();
                int x_position = x+ (getIconWidth() - metrics.stringWidth(""+sortOrderIndex))/2;
                int y_position = y+metrics.getAscent();
                g2.setColor(UIManager.getColor("Table.selectionBackground"));

                g2.drawString(""+sortOrderIndex, x_position+6, y_position-6 );
            }
        }

        @Override
        public int getIconWidth() {
            return undecoratedIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return undecoratedIcon.getIconHeight();
        }

        private void  initUndecoratedIcons(){
            if (sortInfo.getSortOrder() == SortOrder.ASCENDING){
                undecoratedIcon = ascendingIcon;
                disabledUndecoratedIcon = ascendingDIcon;
            }else if (sortInfo.getSortOrder() == SortOrder.DESCENDING){
                undecoratedIcon = descendingIcon;
                disabledUndecoratedIcon = descendingDIcon;
            }
        }


    }

}
