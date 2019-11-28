/*
 * TasksExecutionPanel.java
 *
 * Created on 24 september 2003, 17:14
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.OfflineWorker;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.rows.task.TaskExecutionRow;
import com.energyict.mdc.engine.offline.gui.table.RestrictedTableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.table.TableCreator;
import com.energyict.mdc.engine.offline.gui.table.renderer.MeterReadingTypeTableCellRenderer;
import com.energyict.mdc.engine.offline.gui.table.renderer.TaskStateTableCellRenderer;
import com.energyict.mdc.engine.offline.model.CustomCompletionCode;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Koen, Geert (2014)
 */
public class TaskExecutionPanel extends JPanel {

    private JPanel jPanelAllTasks;
    private JScrollPane jScrollPaneAllTasks;
    private JPanel eastPnl;
    private JButton infoBtn;
    private JButton remoteReadBtn;
    private JButton manualReadBtn;
    private JButton completeBtn;
    private JButton skipBtn;

    /**
     * Indicates if the application is busy with execution of a RMR task
     */
    private AtomicBoolean busyExecutionTask = new AtomicBoolean();

    private static final Log logger = LogFactory.getLog(TaskExecutionPanel.class);

    private static final int ROW_HEIGHT = 25;

    private OfflineWorker guiWorker;
    private TableCreator tableCreatorAllTasks;

    public TaskExecutionPanel(OfflineWorker guiWorker) {
        this.guiWorker = guiWorker;
        initComponents();
        initTableAllTasks();
    }

    private void initTableAllTasks() {
        try {
            tableCreatorAllTasks = new TableCreator(TaskExecutionRow.class);
            tableCreatorAllTasks.setRowHeight(ROW_HEIGHT);
            tableCreatorAllTasks.getJTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableCreatorAllTasks.getJTable().setDefaultRenderer(MeterReadingType.class, new MeterReadingTypeTableCellRenderer());
            tableCreatorAllTasks.getJTable().setDefaultRenderer(TaskState.class, new TaskStateTableCellRenderer());
            jScrollPaneAllTasks.setViewportView(tableCreatorAllTasks.getJTable());
            tableCreatorAllTasks.getJTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    updateButtons();
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public TableCreator getTableCreatorAllTasks() {
        return tableCreatorAllTasks;
    }

    private void initComponents() {

        jScrollPaneAllTasks = new JScrollPane();

        jPanelAllTasks = new JPanel();
        jPanelAllTasks.setLayout(new BorderLayout());
        jPanelAllTasks.add(jScrollPaneAllTasks, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(jPanelAllTasks, BorderLayout.CENTER);
        JPanel keepUpPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keepUpPnl.add(getEastPnl());
        add(keepUpPnl, BorderLayout.EAST);
    }

    public void initializeRows() {
        guiWorker.getTaskManager().initiateExecutionRowsBuild();
        tableCreatorAllTasks.setRows(guiWorker.getTaskManager().getTaskExecutionRows());
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
            eastPnl.add(getRemoteReadBtn(), gbc);

            gbc.gridy++;
            eastPnl.add(getManualReadBtn(), gbc);

            gbc.gridy++;
            eastPnl.add(getCompleteBtn(), gbc);

            gbc.gridy++;
            eastPnl.add(getSkipBtn(), gbc);
        }
        return eastPnl;
    }

    private JButton getSkipBtn() {
        if (skipBtn == null) {
            skipBtn = new JButton(UiHelper.translate("skipTask"));
            skipBtn.setMnemonic(KeyEvent.VK_K);
            skipBtn.setEnabled(false);
            skipBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!getSkipBtn().isEnabled()) {
                        return;
                    }
                    CustomCompletionCode code = UiHelper.getCustomCompletionCode();
                    if (code == null) {
                        return; // user pressed cancel/esc
                    }
                    TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
                    if (selectedRow != null) {
                        selectedRow.skip(code);
                    }
                }
            });
        }
        return skipBtn;
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
                    TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
                    if (selectedRow != null) {
                        selectedRow.showInfo(guiWorker.getOfflineFrame());
                    }
                }
            });
        }
        return infoBtn;
    }

    private JButton getCompleteBtn() {
        if (completeBtn == null) {
            completeBtn = new JButton(UiHelper.translate("complete"));
            completeBtn.setMnemonic(KeyEvent.VK_C);
            completeBtn.setEnabled(false);
            completeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!getCompleteBtn().isEnabled()) {
                        return;
                    }
                    CustomCompletionCode customCompletionCode = UiHelper.getCustomCompletionCode();
                    if (customCompletionCode == null) {
                        return; // user pressed cancel/esc
                    }
                    TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
                    if (selectedRow != null) {
                        selectedRow.complete(customCompletionCode);
                        UiHelper.getMainWindow().invokeUpdateConfigPanel();
                    }
                }
            });
        }
        return completeBtn;
    }

    private JButton getRemoteReadBtn() {
        if (remoteReadBtn == null) {
            remoteReadBtn = new JButton(UiHelper.translate("mmr.remoteRead"));
            remoteReadBtn.setMnemonic(KeyEvent.VK_R);
            remoteReadBtn.setIcon(MdwIcons.REMOTE_METER_READ_ICON);
            remoteReadBtn.setDisabledIcon(MdwIcons.REMOTE_METER_READ_ICON_DISABLED);
            remoteReadBtn.setEnabled(false);
            remoteReadBtn.addActionListener(getRemoteReadActionListener());
        }
        return remoteReadBtn;
    }

    private ActionListener getRemoteReadActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!getRemoteReadBtn().isEnabled()) {
                    return;
                }
                if (!isRowSelected()) {
                    return;
                }
                TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
                if (TaskState.EXECUTING.equals(selectedRow.getState())) {
                    selectedRow.getComJobExecutionModel().setState(ComJobState.Aborting);
                    selectedRow.abortRemoteRead();
                } else {
                    busyExecutionTask.set(true);
                    selectedRow.triggerRemoteRead();
                }
            }
        };
    }

    private JButton getManualReadBtn() {
        if (manualReadBtn == null) {
            manualReadBtn = new JButton(UiHelper.translate("mmr.manualRead"));
            manualReadBtn.setMnemonic(KeyEvent.VK_M);
            manualReadBtn.setEnabled(false);
            manualReadBtn.setIcon(MdwIcons.MANUAL_METER_READ_ICON);
            manualReadBtn.setDisabledIcon(MdwIcons.MANUAL_METER_READ_ICON_DISABLED);
            manualReadBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!getManualReadBtn().isEnabled()) {
                        return;
                    }
                    if (!isRowSelected()) {
                        return;
                    }
                    TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
                    selectedRow.triggerManualRead();
                }
            });
        }
        return manualReadBtn;
    }

    private void updateButtons() {
        getInfoBtn().setEnabled(isRowSelected());
        updateBusyFlag();
        updateReadBtns();
        updateSkipBtn();
        updateCompleteBtn();
    }

    private void updateBusyFlag() {
        for (Object taskExecutionRow : tableCreatorAllTasks.getRows()) {
            if (TaskState.EXECUTING.equals(((TaskExecutionRow) taskExecutionRow).getState()) ||
                    TaskState.ABORTING.equals(((TaskExecutionRow) taskExecutionRow).getState())) {
                busyExecutionTask.set(true);
                return;
            }
        }
        busyExecutionTask.set(false);
    }

    private void updateSkipBtn() {
        if (!isRowSelected()) {
            getSkipBtn().setEnabled(false);
            return;
        }
        TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
        getSkipBtn().setEnabled(selectedRow.getResult() == ComJobResult.Pending);
    }

    private void updateCompleteBtn() {
        if (!isRowSelected()) {
            getCompleteBtn().setEnabled(false);
            return;
        }
        TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
        getCompleteBtn().setEnabled(selectedRow.getResult() != ComJobResult.Pending);
    }

    private void updateReadBtns() {
        if (!isRowSelected()) {
            getRemoteReadBtn().setEnabled(false);
            getRemoteReadBtn().setText(UiHelper.translate("mmr.remoteRead"));
            getManualReadBtn().setEnabled(false);
            return;
        }
        TaskExecutionRow selectedRow = getSelectedTaskExecutionRow();
        if (selectedRow.getState().equals(TaskState.EXECUTING)) {
            getRemoteReadBtn().setEnabled(true);
            getRemoteReadBtn().setText(UiHelper.translate("abort"));
        } else {
            getRemoteReadBtn().setEnabled(!busyExecutionTask.get() && selectedRow.canBeReadRemote());
            getRemoteReadBtn().setText(UiHelper.translate(selectedRow.getResult() == ComJobResult.Failed ? "mmr.remoteReadRetry" : "mmr.remoteRead"));
        }

        getManualReadBtn().setEnabled(selectedRow.canBeReadManual());
    }

    private boolean isRowSelected() {
        return tableCreatorAllTasks.getJTable().getSelectedRow() != -1;
    }

    private TaskExecutionRow getSelectedTaskExecutionRow() {
        int selectedRow = tableCreatorAllTasks.getJTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        JTable table = tableCreatorAllTasks.getJTable();
        List rows = tableCreatorAllTasks.getRows();
        int realIndex = ((RestrictedTableBubbleSortDecorator) table.getModel()).getRealModelRow(selectedRow);
        return (TaskExecutionRow) rows.get(realIndex);
    }
}