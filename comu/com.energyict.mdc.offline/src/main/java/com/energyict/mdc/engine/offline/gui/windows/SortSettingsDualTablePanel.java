package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.CommonIcons;
import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.model.SortSettingsDualTableModel;
import com.jidesoft.grid.DualTable;
import com.jidesoft.grid.ListTableModelAdapter;
import com.jidesoft.grid.TableModelAdapter;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.JideTitledBorder;
import com.jidesoft.swing.PartialEtchedBorder;
import com.jidesoft.swing.PartialSide;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 21:16
 */
public class SortSettingsDualTablePanel extends JPanel implements IconProvider {

    private static Icon ascendingIcon = CommonIcons.UP_ICON;
    private static Icon descendingIcon = CommonIcons.DOWN_ICON;

    private com.energyict.mdc.engine.offline.core.Translator translator;

    private SortSettingsDualTableModel model;
    private boolean canceled = true;
    private int visibleRowCount;

    public SortSettingsDualTablePanel(SortSettingsDualTableModel model) {
        super(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(6, 3, 3, 3));
        this.model = model;
        visibleRowCount = Math.min(Math.max(model.size(), 5) + 1, 11);
        initComponents();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void doClose() {
        if (getRootPane() == null || getRootPane().getParent() == null) {
            return;
        }
        if (getRootPane().getParent() instanceof JInternalFrame) {
            JInternalFrame parentFrame = (JInternalFrame) (getRootPane().getParent());
            parentFrame.setVisible(false);
            parentFrame.dispose();
        } else {
            JDialog parentDialog = (JDialog) (getRootPane().getParent());
            parentDialog.setVisible(false);
            parentDialog.dispose();
        }
    }

    private void initComponents() {
        this.add(getDualListPanel(), BorderLayout.CENTER);
        this.add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private JComponent getDualListPanel() {
        TableModelAdapter adapter = new TableModelAdapter() {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int column) {
                return null;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return SortOrder.class;
                    default:
                        return Object.class;
                }
            }
        };

        DualTable dualTable = new DualTable(model, adapter) {
            @Override
            protected ListTableModelAdapter createTableModel(ListModel listModel, TableModelAdapter tableModelAdapter, boolean isOriginalTable) {
                if (isOriginalTable){
                    return super.createTableModel(listModel, tableModelAdapter, isOriginalTable);
                }else{
                    return new ListTableModelAdapter(listModel, tableModelAdapter){
                        @Override
                        public boolean isCellEditable(int rowIndex, int columnIndex){
                            return columnIndex == 1;
                        }
                    };
                }
            }

            @Override
            protected void setupTable(JTable table, boolean originalTable) {
                super.setupTable(table, originalTable);
                table.putClientProperty(JideSwingUtilities.SET_OPAQUE_RECURSIVELY_EXCLUDED, Boolean.TRUE);
                table.setTableHeader(null);   // do look like a List
                table.setShowHorizontalLines(false);  // do look like a List
                table.setPreferredScrollableViewportSize(new Dimension(200,(table.getRowMargin()+table.getRowHeight())*visibleRowCount));
                if (originalTable) {
                    table.getColumnModel().removeColumn(table.getColumnModel().getColumn(1));  // Only show columnname
                }else {
                    TableColumn sortOrderColumn = table.getColumnModel().getColumn(1);
                    sortOrderColumn.setMaxWidth(45);
                    sortOrderColumn.setCellRenderer(new SortOrderTableCellRenderer());
                    sortOrderColumn.setCellEditor(new SortOrderTableCellEditor());
                }
            }
        };

        JideTitledBorder borderOriginal = new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), getTranslation("availableColumns"));
        borderOriginal.setTitleFont(borderOriginal.getTitleFont().deriveFont(Font.BOLD));
        dualTable.getOriginalTablePane().setBorder(new CompoundBorder(borderOriginal, new EmptyBorder(3, 0, 0, 0)));
        JideTitledBorder borderSelected = new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), getTranslation("columnsToSortOn"));
        borderSelected.setTitleFont(borderOriginal.getTitleFont().deriveFont(Font.BOLD));
        dualTable.getSelectedTablePane().setBorder(new CompoundBorder(borderSelected, new EmptyBorder(3, 0, 0, 0)));

        dualTable.setButtonVisible(DualTable.COMMAND_MOVE_ALL_LEFT, false);
        dualTable.setButtonVisible(DualTable.COMMAND_MOVE_ALL_RIGHT, false);

        return dualTable;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel buttonContainer = new JPanel(new GridLayout(1, 0, 6, 0));
        JButton applyButton = new JButton(getTranslation("apply"));
        applyButton.setToolTipText(getTranslation("apply"));
        applyButton.setMnemonic(KeyEvent.VK_A);
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canceled = false;
                doClose();
            }
        });

        JButton cancelButton = new JButton(getTranslation("cancel"));
        cancelButton.setToolTipText(getTranslation("cancel"));
        cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doClose();
            }
        });

        buttonContainer.add(applyButton);
        buttonContainer.add(cancelButton);

        buttonPanel.add(buttonContainer);
        return buttonPanel;
    }

    private com.energyict.mdc.engine.offline.core.Translator getTranslator() {
        if (translator == null) {
            translator = TranslatorProvider.instance.get().getTranslator();
        }
        return translator;
    }

    private String getTranslation(String string) {
        return getTranslator().getTranslation(string);
    }

    private class SortOrderTableCellEditor extends DefaultCellEditor {

        SortOrderTableCellEditor() {
            super(new JComboBox<>(SortOrder.values()));
            this.clickCountToStart = 1;
            initCombo();
        }
        @SuppressWarnings("unchecked")
        private void initCombo(){
            JComboBox<SortOrder> combo = (JComboBox<SortOrder>) editorComponent;
            combo.setRenderer(new DefaultListCellRenderer(){
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setText(null);
                    if (value != null) {
                        SortOrder order = (SortOrder) value;
                        setToolTipText(getTranslation(order.toString()));
                        switch (order) {
                            case ASCENDING:
                                setIcon(ascendingIcon);
                                break;
                            case DESCENDING:
                                setIcon(descendingIcon);
                                break;
                            case UNSORTED:
                        }
                    }
                    return this;
                }
            });

        }
    }

    // IconProvider implementation
    public Icon getIcon() {
        return MdwIcons.SORTING_ICON;
    }

    private class SortOrderTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(null);
            if (value != null) {
                SortOrder order = (SortOrder) value;
                setToolTipText(getTranslation(order.toString()));
                switch (order) {
                    case ASCENDING:
                        setIcon(ascendingIcon);
                        break;
                    case DESCENDING:
                        setIcon(descendingIcon);
                        break;
                    case UNSORTED:
                }
            }
            return this;
        }
    }

}