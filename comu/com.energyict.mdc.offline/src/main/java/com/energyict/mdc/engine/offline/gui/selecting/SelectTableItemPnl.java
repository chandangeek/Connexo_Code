package com.energyict.mdc.engine.offline.gui.selecting;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.beans.TableBuilder;
import com.energyict.mdc.engine.offline.gui.dialogs.DataEditingPnl;
import com.energyict.mdc.engine.offline.gui.models.AspectTableModel;
import com.energyict.mdc.engine.offline.gui.table.TableUtils;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


public class SelectTableItemPnl<T> extends EisPropsPnl implements DataEditingPnl {

    private T selectedItem;

    private boolean canceled = false;
    private boolean showNoneBtn = false;
    private boolean readOnlyMode = false;

    protected JTable selectionTable;
    protected TableRowSorter<AspectTableModel<T>> rowSorter;
    protected TableCellRenderer iconRenderer;

    public SelectTableItemPnl(List<T> items, Class<T> classPara, T preselection, List<String> columns) {
        this(items, classPara, preselection, columns, false);
    }

    public SelectTableItemPnl(List<T> items, Class<T> classPara, T preselection, List<String> columns, boolean showNoneBtn) {
        this(items, classPara, preselection, columns, showNoneBtn, false);
    }

    public SelectTableItemPnl(List<T> items, Class<T> classPara, T preselection, List<String> columnsToShow, boolean showNoneBtn, boolean readOnly) {
        super(new BorderLayout());
        this.showNoneBtn = showNoneBtn;
        this.readOnlyMode = readOnly;

        initComponents(new TableBuilder<>(items, getColumnsToShow(classPara, columnsToShow), classPara));
        preselectItem(preselection);
    }

    private List<String> getColumnsToShow(Class<? extends T> classPara, List<String> columnsToShow){
        List<String> columnNames = new ArrayList<>(columnsToShow);
//        boolean showIcon = BusinessObject.class.isAssignableFrom(classPara);
//        if (showIcon) {
//            if (!columnNames.contains("businessObject")) {
//                columnNames.add(0, "businessObject");
//                iconRenderer = new BusinessObjectTypeTableCellRenderer();
//            } // add an icons column
//        } else {
//            showIcon = IconProvider.class.isAssignableFrom(classPara);
//            if (showIcon) {
//                if (!columnNames.contains("icon")) {
//                    columnNames.add(0, "icon");
//                }
//                iconRenderer = new IconTableCellRenderer();
//            } // add an icons column
//        }
        return columnNames;
    }
    @SuppressWarnings("unchecked")
    private void initComponents(TableBuilder<T> tableBuilder)  {
        selectionTable = tableBuilder.getTable();

        rowSorter = new TableRowSorter<>((AspectTableModel<T>) selectionTable.getModel());
        rowSorter.setMaxSortKeys(1);
        if (iconRenderer != null) {
            rowSorter.setSortable(0,false);
            rowSorter.toggleSortOrder(1);
        } else {
            rowSorter.toggleSortOrder(0);
        }
        selectionTable.setRowSorter(rowSorter);
        selectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        selectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
                    tableClicked(selectionTable);
                }
            }
        });

        TableUtils.customize(selectionTable);
        TableUtils.initColumnSizes(selectionTable, Math.min(30, selectionTable.getRowCount()));
        if (iconRenderer != null){
            selectionTable.getColumnModel().getColumn(0).setCellRenderer(iconRenderer);
            selectionTable.getColumnModel().getColumn(0).setMaxWidth(18);
            selectionTable.getColumnModel().getColumn(0).setHeaderValue(null);
            selectionTable.getColumnModel().getColumn(0).setResizable(false);
        }
        add(new JScrollPane(selectionTable), BorderLayout.CENTER);
        add(getButtonPnl(), BorderLayout.SOUTH);
    }

    public void setTableCellRenderer(int columnModelIndex, TableCellRenderer renderer) {
        if (columnModelIndex < selectionTable.getColumnModel().getColumnCount()) {
            selectionTable.getColumnModel().getColumn(columnModelIndex).setCellRenderer(renderer);
        }
        TableUtils.initColumnSizes(selectionTable, Math.min(30, selectionTable.getRowCount()));
    }

    protected JPanel getButtonPnl() {
        JButton noneBtn = new JButton();
        noneBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedItem = null;
                canceled = false;
                doClose();
            }
        });
        noneBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("none"));
        noneBtn.setMnemonic(KeyEvent.VK_S);

        final JButton selectBtn = new JButton();
        selectBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                storeSelection();
                doClose();
            }
        });
        selectBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("select"));
        selectBtn.setMnemonic(KeyEvent.VK_S);
        selectBtn.setEnabled(false);

        final JButton cancelBtn = new JButton();
        if (readOnlyMode) {
            cancelBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("close"));
            cancelBtn.setMnemonic(KeyEvent.VK_C);
        } else {
            cancelBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        }
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               performEscapeAction();
            }
        });

        JPanel buttonPnl = new JPanel(new GridLayout(1, 0, 6, 0));
        if (!readOnlyMode) {
            if (showNoneBtn) {
                buttonPnl.add(noneBtn);
            }
            buttonPnl.add(selectBtn);
            selectionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectBtn.setEnabled(selectionTable.getSelectedRowCount() > 0);
                }
            });
        }
        buttonPnl.add(cancelBtn);

        JPanel southPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPnl.add(buttonPnl);
        return southPnl;
    }
    @SuppressWarnings("unchecked")
    protected void storeSelection() {
        selectedItem = (selectionTable.getSelectedRow() == -1) ? null :  ((AspectTableModel<T>)selectionTable.getModel()).getObjectAt(rowSorter.convertRowIndexToModel(selectionTable.getSelectedRow()));
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    @SuppressWarnings("unchecked")
    public void preselectItem(T itemToPreselect) {
        selectionTable.clearSelection();
        if (itemToPreselect != null) {
            int itemIndex = (((AspectTableModel<T>) selectionTable.getModel()).getModels().indexOf(itemToPreselect));
            if (itemIndex >= 0){
                selectionTable.addRowSelectionInterval(rowSorter.convertRowIndexToView(itemIndex), rowSorter.convertRowIndexToView(itemIndex));
            }
        }
        selectedItem = null;
    }

    protected void tableClicked(JTable table) {
        storeSelection();
        doClose();
    }

    public boolean isCanceled() {
        return canceled;
    }

    // DataEditingPnl interface
    public boolean isDataDirty() {
        return false;
    }

    public void performEscapeAction() {
        selectedItem = null;
        canceled = true;
        doClose();
    }

    public boolean performEnterAction(KeyEvent evt) {
        return false;
    }
}