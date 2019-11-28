/*
 * SortingSettingsPnl.java
 *
 * Created on 13 december 2004, 14:00
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.CommonIcons;
import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.energyict.mdc.engine.offline.gui.models.SortInfoTableModel;
import com.energyict.mdc.engine.offline.gui.table.TableUtils;
import com.energyict.mdc.engine.offline.gui.table.renderer.AscendingTableCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SortingSettingsPnl extends EisPropsPnl {

    private SortSettings sortSettings;
    private List possibleItems;
    private DefaultListModel modelLeft;
    private SortInfoTableModel modelRight;
    private boolean canceled = false;

    public SortingSettingsPnl(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
        possibleItems = new ArrayList(sortSettings.getColumnNames());
        possibleItems.removeAll(sortSettings.getSortColumnNames());
        initialize();
    }

    private void initialize() {
        initComponents();
        initLists();
        updateUpButton();
        updateDownButton();
        updateNoSortButton();
        updateSortButton();
    }

    public boolean isCanceled() {
        return canceled;
    }

    // ------------------------------------------------------------
    /* This function will be triggered if <ESCape> is pressed
     * in the EisDialog containing this panel */

    public void performEscapeAction() {
        canceled = true;
        doClose();
    }

    private void initLists() {
        // Left list
        modelLeft = new DefaultListModel();
        Iterator it = possibleItems.iterator();
        while (it.hasNext()) {
            modelLeft.addElement((String) it.next());
        }
        possibleList.setModel(modelLeft);
        sortPossibleList();

        // Right table
        modelRight = new SortInfoTableModel(sortSettings.getSortInfo());
        sortTable.setModel(modelRight);
        sortTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableUtils.customize(sortTable);
        sortTable.getColumnModel().getColumn(1).
                setCellRenderer(new AscendingTableCellRenderer());
        sortTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        //Ignore extra messages.
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        updateNoSortButton();
                        updateUpButton();
                        updateDownButton();
                    }
                }
        );
    }

    private void sortPossibleList() {
        List sorted = new ArrayList();
        DefaultListModel model = (DefaultListModel) possibleList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            sorted.add((String) model.getElementAt(i));
        }
        Collections.sort(sorted); // sort the translated strings
        model.clear();
        for (int i = 0; i < sorted.size(); i++) {
            model.addElement((String) sorted.get(i));
        }
        possibleList.setModel(model);
    }

    private void updateUpButton() {
        // condition: Adjacent items selected + first selected is not the first
        ListSelectionModel lsm = sortTable.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        boolean enabled = false;
        if (min == max) {
            if (min == -1) {
                enabled = false;
            } else {
                enabled = (min > 0);
            }
        } else {
            enabled = isSelectionSingleInterval(lsm) && min > 0;
        }
        upButton.setEnabled(enabled);
    }

    private void updateDownButton() {
        // condition: Adjacent items selected + last selected is not the last
        ListSelectionModel lsm = sortTable.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        boolean enabled = false;
        if (min == max) {
            if (min == -1) {
                enabled = false;
            } else {
                enabled = (min < modelRight.getRowCount() - 1);
            }
        } else {
            enabled = isSelectionSingleInterval(lsm) &&
                    (max < modelRight.getRowCount() - 1);
        }
        downButton.setEnabled(enabled);
    }

    private boolean isSelectionSingleInterval(ListSelectionModel lsm) {
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (!lsm.isSelectedIndex(i)) {
                return false;
            }
        }
        return true;
    }

    private void updateSortButton() {
        ListSelectionModel lsm = possibleList.getSelectionModel();
        sortButton.setEnabled(lsm.getMinSelectionIndex() != -1);
    }

    private void updateNoSortButton() {
        ListSelectionModel lsm = sortTable.getSelectionModel();
        noSortButton.setEnabled(lsm.getMinSelectionIndex() != -1);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new JPanel();
        Panel3 = new JPanel();
        leftPanel = new JPanel();
        possibleListPanel = new JPanel();
        jScrollPane1 = new JScrollPane();
        possibleList = new JList();
        possibleButtonPanel = new JPanel();
        jPanel1 = new JPanel();
        jPanel3 = new JPanel();
        sortButton = new JButton();
        sortButton.setIcon(CommonIcons.RIGHT_ICON);
        sortButton.setDisabledIcon(CommonIcons.RIGHT_DISABLED_ICON);
        noSortButton = new JButton();
        noSortButton.setIcon(CommonIcons.LEFT_ICON);
        noSortButton.setDisabledIcon(CommonIcons.LEFT_DISABLED_ICON);
        rightPanel = new JPanel();
        sortListPanel = new JPanel();
        jScrollPane2 = new JScrollPane();
        sortTable = new JTable();
        shownbuttonPanel = new JPanel();
        jPanel2 = new JPanel();
        jPanel4 = new JPanel();
        upButton = new JButton();
        upButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("up"));
        upButton.setMnemonic(KeyEvent.VK_U);
        upButton.setIcon(CommonIcons.UP_ICON);
        upButton.setDisabledIcon(CommonIcons.UP_DISABLED_ICON);
        downButton = new JButton();
        downButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("down"));
        downButton.setMnemonic(KeyEvent.VK_D);
        downButton.setIcon(CommonIcons.DOWN_ICON);
        downButton.setDisabledIcon(CommonIcons.DOWN_DISABLED_ICON);

        southPanel = new JPanel();
        buttonPanel = new JPanel();
        applyButton = new JButton();
        applyButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("apply"));
        applyButton.setMnemonic(KeyEvent.VK_A);
        cancelButton = new JButton();
        cancelButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        Panel3.setLayout(new java.awt.GridLayout(1, 0));

        leftPanel.setLayout(new java.awt.BorderLayout());

        possibleListPanel.setLayout(new java.awt.BorderLayout());

        possibleListPanel.setBorder(new javax.swing.border.TitledBorder(" " + TranslatorProvider.instance.get().getTranslator().getTranslation("availableColumns") + " "));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 250));
        possibleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                possibleListValueChanged(evt);
            }
        });
        possibleList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                possibleListMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(possibleList);

        possibleListPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        leftPanel.add(possibleListPanel, java.awt.BorderLayout.CENTER);

        possibleButtonPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 2));

        jPanel3.setLayout(new java.awt.GridLayout(0, 1, 0, 6));

        sortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        });

        jPanel3.add(sortButton);

        noSortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noSortButtonActionPerformed(evt);
            }
        });

        jPanel3.add(noSortButton);

        jPanel1.add(jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        possibleButtonPanel.add(jPanel1, gridBagConstraints);

        leftPanel.add(possibleButtonPanel, java.awt.BorderLayout.EAST);

        Panel3.add(leftPanel);

        rightPanel.setLayout(new java.awt.BorderLayout());

        rightPanel.setBorder(new javax.swing.border.TitledBorder(" " + TranslatorProvider.instance.get().getTranslator().getTranslation("columnsToSortOn") + " "));
        sortListPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 250));
        sortTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null},
                        {null, null},
                        {null, null},
                        {null, null}
                },
                new String[]{
                        "Title 1", "Title 2"
                }
        ));
        sortTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sortTableMouseClicked(evt);
            }
        });

        jScrollPane2.setViewportView(sortTable);

        sortListPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        rightPanel.add(sortListPanel, java.awt.BorderLayout.CENTER);

        shownbuttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));

        jPanel4.setLayout(new java.awt.GridLayout(0, 1, 0, 6));

        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        jPanel4.add(upButton);

        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        jPanel4.add(downButton);

        jPanel2.add(jPanel4);

        shownbuttonPanel.add(jPanel2);

        rightPanel.add(shownbuttonPanel, java.awt.BorderLayout.EAST);

        Panel3.add(rightPanel);

        mainPanel.add(Panel3, java.awt.BorderLayout.CENTER);

        add(mainPanel, java.awt.BorderLayout.CENTER);

        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));

        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(applyButton);

        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        southPanel.add(buttonPanel);

        add(southPanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void sortTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sortTableMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1) {
            java.awt.Point pt = new java.awt.Point(evt.getX(), evt.getY());
            if (sortTable.columnAtPoint(pt) == 1) {
                int row = sortTable.rowAtPoint(pt);
                SortInfo info = (SortInfo) modelRight.getObjectAt(row);
                info.setAscending(!info.getAscending());
                modelRight.fireTableRowsUpdated(row, row);
            }
        }
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
            noSortButtonActionPerformed(null);
        }
    }//GEN-LAST:event_sortTableMouseClicked

    private void possibleListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_possibleListMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
            sortButtonActionPerformed(null);
        }
    }//GEN-LAST:event_possibleListMouseClicked

    private void possibleListValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_possibleListValueChanged
        updateSortButton();
    }//GEN-LAST:event_possibleListValueChanged

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        performEscapeAction();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        doClose();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        ListSelectionModel lsm = sortTable.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        SortInfo info = (SortInfo) modelRight.getObjectAt(max + 1);
        modelRight.removeElementAt(max + 1);
        modelRight.insertElementAt(info, min);
        lsm.setSelectionInterval(min + 1, max + 1);
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        ListSelectionModel lsm = sortTable.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        SortInfo info = (SortInfo) modelRight.getObjectAt(min - 1);
        modelRight.removeElementAt(min - 1);
        modelRight.insertElementAt(info, max);
        lsm.setSelectionInterval(min - 1, max - 1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void noSortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noSortButtonActionPerformed
        ListSelectionModel lsmS = sortTable.getSelectionModel();
        int min = lsmS.getMinSelectionIndex();
        int max = lsmS.getMaxSelectionIndex();
        int indexToInsert = possibleList.getModel().getSize();
        for (int i = max; i >= min; i--) {
            if (!lsmS.isSelectedIndex(i)) {
                continue;
            }
            SortInfo info = (SortInfo) modelRight.getObjectAt(i);
            modelRight.removeElementAt(i);
            modelLeft.insertElementAt(info.getName(), indexToInsert);
        }
        sortPossibleList();
        // GD 2004-jul-06 Don't allow to hide all columns:
  //      applyButton.setEnabled(modelRight.getRowCount() > 0);
    }//GEN-LAST:event_noSortButtonActionPerformed

    private void sortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButtonActionPerformed
        ListSelectionModel lsmH = possibleList.getSelectionModel();
        int min = lsmH.getMinSelectionIndex();
        int max = lsmH.getMaxSelectionIndex();
        String str = null;
        int indexToInsert = sortTable.getModel().getRowCount();
        for (int i = max; i >= min; i--) {
            if (!lsmH.isSelectedIndex(i)) {
                continue;
            }
            str = (String) modelLeft.getElementAt(i);
            modelLeft.removeElementAt(i);
            modelRight.insertElementAt(new SortInfo(str), indexToInsert);
        }
        // GD 2004-jul-06 Don't allow to hide all columns:
  //      applyButton.setEnabled(modelRight.getRowCount() > 0);
    }//GEN-LAST:event_sortButtonActionPerformed

    @Override
    public void doClose() {
        if (getRootPane() == null || getRootPane().getParent() == null) {
            return;
        }
        if (getRootPane().getParent() instanceof JInternalFrame) {
            JInternalFrame parentFrame = (JInternalFrame) (getRootPane().getParent());
            parentFrame.doDefaultCloseAction();
        } else {
            JDialog parentDialog = (JDialog) (getRootPane().getParent());
            parentDialog.setVisible(false);
            parentDialog.dispose();
        }
    }

    @Override
    public Icon getIcon() {
        return MdwIcons.SORTING_ICON;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel Panel3;
    private JButton applyButton;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton downButton;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JPanel leftPanel;
    private JPanel mainPanel;
    private JButton noSortButton;
    private JPanel possibleButtonPanel;
    private JList possibleList;
    private JPanel possibleListPanel;
    private JPanel rightPanel;
    private JPanel shownbuttonPanel;
    private JButton sortButton;
    private JPanel sortListPanel;
    private JTable sortTable;
    private JPanel southPanel;
    private JButton upButton;
    // End of variables declaration//GEN-END:variables

}
