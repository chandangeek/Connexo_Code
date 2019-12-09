/*
 * TaskManagementPanel.java
 *
 * Created on 24 september 2003, 17:14
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.offline.core.OfflineWorker;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.rows.task.TaskManagementRow;
import com.energyict.mdc.engine.offline.gui.table.RestrictedTableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.table.TableCreator;
import com.energyict.mdc.engine.offline.gui.table.renderer.MeterReadingTypeTableCellRenderer;
import com.energyict.mdc.engine.offline.gui.table.renderer.TaskStateTableCellRenderer;
import com.energyict.mdc.engine.offline.model.MeterReadingType;
import com.energyict.mdc.engine.offline.model.TaskState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen, Geert (2014)
 *         Changes 14052004 construct a nice popup in case of databaseexception
 */
public class TaskManagementPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private JScrollPane jScrollPaneCenter;
    private JPanel eastPnl;
    private JButton infoBtn;
    private JButton resetBtn;
    private JButton uploadBtn;

    private static final Log logger = LogFactory.getLog(TaskManagementPanel.class);
    private static final int ROW_HEIGHT = 25;

    private OfflineWorker guiWorker;
    private TableCreator tableCreator;

    public TaskManagementPanel(OfflineWorker guiWorker) {
        this.guiWorker = guiWorker;
        this.guiWorker.getOfflineFrame().addPropertyChangeListener(this);
        initComponents();
        initTable();
    }

    private void initTable() {
        try {
            tableCreator = new TableCreator(TaskManagementRow.class);
            tableCreator.setRowHeight(ROW_HEIGHT);
            JTable table = tableCreator.getJTable();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    updateButtons();
                }
            });
            table.setDefaultRenderer(MeterReadingType.class, new MeterReadingTypeTableCellRenderer());
            table.setDefaultRenderer(TaskState.class, new TaskStateTableCellRenderer());
            jScrollPaneCenter.setViewportView(table);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public TableCreator getTableCreator() {
        return tableCreator;
    }

    private void initComponents() {
        jScrollPaneCenter = new JScrollPane();
        jScrollPaneCenter.setMinimumSize(null);
        jScrollPaneCenter.setPreferredSize(null);

        setLayout(new BorderLayout());
        add(jScrollPaneCenter, BorderLayout.CENTER);
        JPanel keepUpPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keepUpPnl.add(getEastPnl());
        add(keepUpPnl, BorderLayout.EAST);
    }

    private JPanel getEastPnl() {
        if (eastPnl == null) {
            eastPnl = new JPanel(new GridBagLayout());
            eastPnl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 2, 5, 2);
            gbc.gridx = 0;
            gbc.gridy = 0;
            eastPnl.add(getInfoBtn(), gbc);

            gbc.gridy++;
            eastPnl.add(getResetBtn(), gbc);

            gbc.gridy++;
            eastPnl.add(getUploadBtn(), gbc);
        }
        return eastPnl;
    }

    private JButton getInfoBtn() {
        if (infoBtn == null) {
            infoBtn = new JButton(UiHelper.translate("jButtonTaskInfo"));
            infoBtn.setMnemonic(KeyEvent.VK_I);
            infoBtn.setEnabled(false);
            infoBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!getInfoBtn().isEnabled()) {
                        return;
                    }
                    TaskManagementRow selectedRow = getSelectedTaskManagementRow();
                    if (selectedRow != null) {
                        selectedRow.showInfo(guiWorker.getOfflineFrame());
                    }
                }
            });
        }
        return infoBtn;
    }

    private JButton getResetBtn() {
        if (resetBtn == null) {
            resetBtn = new JButton(UiHelper.translate("resetTaskState"));
            resetBtn.setMnemonic(KeyEvent.VK_R);
            resetBtn.setEnabled(false);
            resetBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TaskManagementRow selectedRow = getSelectedTaskManagementRow();
                    if (selectedRow != null) {
                        selectedRow.doResetTask();
                    }
                }
            });
        }
        return resetBtn;
    }

    private JButton getUploadBtn() {
        if (uploadBtn == null) {
            uploadBtn = new JButton(UiHelper.translate("mmr.upload"));
            uploadBtn.setMnemonic(KeyEvent.VK_U);
            uploadBtn.setEnabled(guiWorker.getOfflineFrame().isCanStoreData());
            uploadBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateUploadBtn(false);
                    guiWorker.getOfflineFrame().doStoreData();
                }
            });
        }
        return uploadBtn;
    }

    public void updateUploadBtn(boolean uploadState) {
        getUploadBtn().setText(uploadState ? UiHelper.translate("mmr.upload") : UiHelper.translate("abort"));
    }

    private TaskManagementRow getSelectedTaskManagementRow() {
        int selectedRow = tableCreator.getJTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        JTable table = tableCreator.getJTable();
        List rows = tableCreator.getRows();
        int realIndex = ((RestrictedTableBubbleSortDecorator) table.getModel()).getRealModelRow(selectedRow);
        return (TaskManagementRow) rows.get(realIndex);
    }

    private void updateButtons() {
        getInfoBtn().setEnabled(isRowSelected());
        updateResetBtn();
        getUploadBtn().setEnabled(guiWorker.getOfflineFrame().isCanStoreData());
    }

    private boolean isRowSelected() {
        return tableCreator.getJTable().getSelectedRow() != -1;
    }

    private void updateResetBtn() {
        if (!isRowSelected()) {
            getResetBtn().setEnabled(false);
            return;
        }
        TaskManagementRow selectedRow = getSelectedTaskManagementRow();
        getResetBtn().setEnabled(selectedRow.getState() == TaskState.READ_SUCCESS || selectedRow.getState() == TaskState.READ_FAILED);
    }

    // Only the ones without tasks to do should be shown/taken into account here
    public void initializeRows() {
        List<TaskManagementRow> allRows = guiWorker.getTaskManager().rebuildTableRows();
        List<TaskManagementRow> rowsToShow = new ArrayList<>();
        for (TaskManagementRow each : allRows) {
            ComJobExecutionModel model = each.getComJobExecutionModel();
            if ((model.getState().equals(ComJobState.AwaitingStore) || model.getState().equals(ComJobState.Storing) || model.getState().equals(ComJobState.Done)) &&
                    model.getCompleted() &&
                    UiHelper.getMainWindow().passesThruFilter(model)) {
                rowsToShow.add(each);
            }
        }
        getTableCreator().setRows(rowsToShow);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (OfflineFrame.PROPERTY_CANSTOREDATA.equals(evt.getPropertyName())) {
            getUploadBtn().setEnabled((boolean) evt.getNewValue());
        }
    }
}