package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.upl.meterdata.LoadProfileType;
import com.energyict.mdc.upl.meterdata.RegisterGroup;
import com.energyict.mdc.tasks.ManualMeterReadingsTask;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineRegister;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jme
 */
public class TaskInfoDialog extends EisDialog {

    private GridBagConstraints fillConstraints =
        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

    private JPanel mainPnl;
    private JPanel centerPnl;
    private JScrollPane jScrollPaneTree;
    private JTree jTreeInfoSelection;
    private JPanel jPanelInfo;
    private JPanel jPannelButtons;
    private JButton jButtonClose;

    private DeviceInfoPnl jPanelDeviceInfo;
    private ComTasksInfoPnl comTasksInfoPnl;
    private ConnectionTaskInfoPnl connectionTaskInfoPnl;
    private DeviceRegistersInfoPnl jPanelDeviceRegistersPnl;
    private LoadProfilesInfoPnl loadProfilesInfoPnl;
    private SecuritySetInfoPnl securitySetInfoPnl;
    private GeneralPropertiesInfoPnl generalPropertiesInfoPnl;

    private ComJobExecutionModel comJobExecutionModel;

    private final String NLS_TASK = UiHelper.translate("task");
    private final String NLS_DEVICEINFO = UiHelper.translate("deviceInformation");
    private final String NLS_COMTASKS = UiHelper.translate("communicationTasks");
    private final String NLS_CONNECTION = UiHelper.translate("connectionTask");
    private final String NLS_SECURITY_SET = UiHelper.translate("securitySet");
    private final String NLS_GENERAL_PROPERTIES = UiHelper.translate("protocol.generalProperties");
    private final String NLS_REGISTERS = UiHelper.translate("devices.registers.title");
    private final String NLS_LOADPROFILES = UiHelper.translate("loadProfiles");
    private final String NLS_CLOSE = UiHelper.translate("close");

    public TaskInfoDialog(Frame ownerFrame, ComJobExecutionModel comJobExecutionModel) {
        super(ownerFrame, UiHelper.translate("taskInformation"), true);
        this.comJobExecutionModel = comJobExecutionModel;
        initComponents();
    }

    private void jTreeInfoSelectionValueChanged(TreeSelectionEvent e) {
        String lastItem = e.getPath().getLastPathComponent().toString();
        jPanelInfo.removeAll();

        if (NLS_DEVICEINFO.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(jPanelDeviceInfo, fillConstraints);
        } else if (NLS_COMTASKS.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(comTasksInfoPnl, fillConstraints);
        } else if (NLS_CONNECTION.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(connectionTaskInfoPnl, fillConstraints);
        } else if (NLS_SECURITY_SET.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(securitySetInfoPnl, fillConstraints);
        } else if (NLS_REGISTERS.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(jPanelDeviceRegistersPnl, fillConstraints);
        } else if (NLS_GENERAL_PROPERTIES.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(generalPropertiesInfoPnl, fillConstraints);
        } else if (NLS_LOADPROFILES.equalsIgnoreCase(lastItem)) {
            jPanelInfo.add(loadProfilesInfoPnl, fillConstraints);
        }
        jPanelInfo.updateUI();
    }

    private void initPanels() {
        ComJobExecutionModel model = getComJobExecutionModel();
        //TODO add panel for dialect properties

        jPanelDeviceInfo = new DeviceInfoPnl(model.getDevice(), model.getOfflineDevice());
        comTasksInfoPnl = new ComTasksInfoPnl(model.getComTaskExecutions());
        connectionTaskInfoPnl = new ConnectionTaskInfoPnl(model.getConnectionTask());
        securitySetInfoPnl = new SecuritySetInfoPnl(
            model.getComTaskEnablementMap(),
            model.getSecuritySetPropertiesMap(),
            model.getComTaskExecutions());
        generalPropertiesInfoPnl = new GeneralPropertiesInfoPnl(model.getOfflineDevice().getAllProperties().toStringProperties());
        jPanelDeviceRegistersPnl = new DeviceRegistersInfoPnl(getRegistersToRead());
        loadProfilesInfoPnl = new LoadProfilesInfoPnl(getLoadProfilesToRead());
    }

    private List<OfflineRegister> getRegistersToRead() {
        List<RegisterGroup> registerGroups = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : getComJobExecutionModel().getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof RegistersTask) {
                    for (RegisterGroup registerGroup : ((RegistersTask) protocolTask).getRegisterGroups()) {
                        if (!registerGroups.contains(registerGroup)) {
                            registerGroups.add(registerGroup);
                        }
                    }
                }
                if (protocolTask instanceof ManualMeterReadingsTask) {  //TODO test this
                    for (RegisterGroup registerGroup : ((ManualMeterReadingsTask) protocolTask).getRegisterGroups()) {
                        if (!registerGroups.contains(registerGroup)) {
                            registerGroups.add(registerGroup);
                        }
                    }
                }
            }
        }

        if (registerGroups.isEmpty()) {
            return getComJobExecutionModel().getOfflineDevice().getAllOfflineRegisters();
        } else {
            return getComJobExecutionModel().getOfflineDevice().getRegistersForRegisterGroup(registerGroups);
        }
    }

    private List<OfflineLoadProfile> getLoadProfilesToRead() {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : getComJobExecutionModel().getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LoadProfilesTask) {
                    for (LoadProfileType loadProfileType : ((LoadProfilesTask) protocolTask).getLoadProfileTypes()) {
                        if (!loadProfileTypes.contains(loadProfileType)) {
                            loadProfileTypes.add(loadProfileType);
                        }
                    }
                }
            }
        }

        if (loadProfileTypes.isEmpty()) {
            return getComJobExecutionModel().getOfflineDevice().getAllOfflineLoadProfiles();
        } else {
            return getComJobExecutionModel().getOfflineDevice().getLoadProfilesForLoadProfileTypes(loadProfileTypes);
        }
    }

    private void initComponents() {

        initPanels();

        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - unknown
        mainPnl = new JPanel();
        centerPnl = new JPanel();
        jScrollPaneTree = new JScrollPane();
        jTreeInfoSelection = new JTree();
        jPanelInfo = new JPanel();
        jPannelButtons = new JPanel();
        jButtonClose = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== mainPnl ========
        {
            mainPnl.setBorder(new EmptyBorder(12, 12, 12, 12));
            mainPnl.setLayout(new BorderLayout());

            //======== centerPnl ========
            {
                GridBagLayout layout = new GridBagLayout();
                layout.columnWidths = new int[]{200, 700, 0};
                layout.rowHeights = new int[]{334, 0};
                layout.columnWeights = new double[]{0.0, 1.0, 1.0E-4};
                layout.rowWeights = new double[]{1.0, 1.0E-4};
                centerPnl.setLayout(layout);

                //======== jScrollPaneTree ========
                {

                    //---- jTreeInfoSelection ----
                    jTreeInfoSelection.setModel(new DefaultTreeModel(
                            new DefaultMutableTreeNode(NLS_TASK) {
                                {
                                    add(new DefaultMutableTreeNode(NLS_DEVICEINFO));
                                    add(new DefaultMutableTreeNode(NLS_COMTASKS));
                                    add(new DefaultMutableTreeNode(NLS_CONNECTION));
                                    add(new DefaultMutableTreeNode(NLS_SECURITY_SET));
                                    add(new DefaultMutableTreeNode(NLS_GENERAL_PROPERTIES));
                                    add(new DefaultMutableTreeNode(NLS_REGISTERS));
                                    add(new DefaultMutableTreeNode(NLS_LOADPROFILES));
                                }
                            }));

                    jTreeInfoSelection.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    jTreeInfoSelection.setBorder(new EmptyBorder(5, 5, 5, 5));
                    jTreeInfoSelection.setRootVisible(false);
                    jTreeInfoSelection.setShowsRootHandles(true);
                    jTreeInfoSelection.addTreeSelectionListener(new TreeSelectionListener() {
                        public void valueChanged(TreeSelectionEvent e) {
                            jTreeInfoSelectionValueChanged(e);
                        }
                    });
                    jScrollPaneTree.setViewportView(jTreeInfoSelection);
                }
                centerPnl.add(jScrollPaneTree, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //======== jPanelInfo ========
                {
                    jPanelInfo.setLayout(new GridBagLayout());
                    ((GridBagLayout) jPanelInfo.getLayout()).columnWidths = new int[]{0, 0};
                    ((GridBagLayout) jPanelInfo.getLayout()).rowHeights = new int[]{0, 0};
                    ((GridBagLayout) jPanelInfo.getLayout()).columnWeights = new double[]{1.0, 1.0E-4};
                    ((GridBagLayout) jPanelInfo.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

                }
                centerPnl.add(jPanelInfo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            mainPnl.add(centerPnl, BorderLayout.CENTER);

            //======== jPannelButtons ========
            {
                jPannelButtons.setBorder(new EmptyBorder(12, 0, 0, 0));
                jPannelButtons.setLayout(new GridBagLayout());
                ((GridBagLayout) jPannelButtons.getLayout()).columnWidths = new int[]{0, 80};
                ((GridBagLayout) jPannelButtons.getLayout()).columnWeights = new double[]{1.0, 0.0};

                //---- jButtonClose ----
                jButtonClose.setText(NLS_CLOSE);
                jButtonClose.setMnemonic(KeyEvent.VK_C);
                jButtonClose.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
                jPannelButtons.add(jButtonClose, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            mainPnl.add(jPannelButtons, BorderLayout.SOUTH);
        }
        contentPane.add(mainPnl, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        jTreeInfoSelection.setSelectionRow(0);

        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public ComJobExecutionModel getComJobExecutionModel() {
        return comJobExecutionModel;
    }
}