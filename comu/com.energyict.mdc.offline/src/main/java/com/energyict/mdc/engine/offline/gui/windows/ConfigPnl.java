package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.offline.OfflineActions;
import com.energyict.mdc.engine.offline.core.QueryDateGetTasks;
import com.energyict.mdc.engine.offline.core.RegistryConfiguration;
import com.energyict.mdc.engine.offline.core.exception.SyncException;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ConfigPnl extends JPanel implements IndeterminateProgressCancel, PropertyChangeListener {

    private static final int WORKING_DIRECTORY_FLD_WIDTH = 200;
    private static final int WORKING_DIRECTORY_FLD_HEIGHT = 20;

    private static final String STORING_COLLECTED_DATA_TITLE = UiHelper.translate("mmr.storingCollectedData") + "...";
    private static final String CONNECT = UiHelper.translate("connect");
    private static final String DISCONNECT = UiHelper.translate("disconnect");
    private static final String STORE_DATA = UiHelper.translate("mmr.upload");
    private static final String ABORT_STORE_DATA = UiHelper.translate("abort");
    private static final String WORKING_DIRECTORY_DATA = UiHelper.translate("mmr.workingDirectoryForDataFiles");
    private static final String WORKING_DIRECTORY_SYSTEM = UiHelper.translate("mmr.workingDirectoryForSystemFiles");
    private static final String CHANGE_WORKING_DIRECTORY = UiHelper.translate("mmr.change");
    private static final String CHANGE_WORKING_DIRECTORY_RESTART_TITLE = UiHelper.translate("mmr.changeWorkingDirectoryTitle");
    private static final String CHANGE_WORKING_DIRECTORY_RESTART_INFO = UiHelper.translate("mmr.changeWorkingDirectoryInfo");
    private static final String WORKING_DIRECTORY = UiHelper.translate("mmr.workingDirectories");
    private static final String COMSERVER_CONNECTION = UiHelper.translate("mmr.comserverConnection");

    private static final String TEMP_SYSTEM_FILES_DIRECTORY = "tempsystemfilesdirectory";
    private static final String TEMP_DATA_FILES_DIRECTORY = "tempdatafilesdirectory";

    private JPanel queryDatePnl;
    private JButton getTasksBtn;
    private JButton connectBtn;
    private JButton storeDataBtn;
    private JLabel serverInfoLbl;
    private JPanel mainPnl;

    private JPanel workingDirPnl;
    private JTextField workingDirDataFld;
    private JButton workingDirDataBtn;
    private JTextField workingDirSystemFld;
    private JButton workingDirSystemBtn;
    private JFileChooser workingDirFileChr;

    private final OfflineFrame mainFrame;
    private final QueryDateGetTasks queryDate = new QueryDateGetTasks(new Date()); // initialize with right now
    private final FormBuilder queryDateFormBuilder;
    private IndeterminateProgress indeterminateProgress;
    private AtomicBoolean storingWasCanceled = new AtomicBoolean(false);
    private boolean isConnected = false;

    public ConfigPnl(OfflineFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.mainFrame.addPropertyChangeListener(this);
        queryDateFormBuilder = new FormBuilder<>(queryDate);
        queryDatePnl = queryDateFormBuilder.getPanel("queryDate");

        initComponents();
        toggleControls();

        // To avoid the first tab having the focus at startup
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                UiHelper.setDefaultFocusInWindow(ConfigPnl.this);
            }
        });
    }

    public Component getDefaultFocusedComponent() {
        return queryDatePnl.getComponent(0);
    }

    private void initComponents() {

        setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel insidePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel keepLeftPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keepLeftPnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(COMSERVER_CONNECTION), BorderFactory.createEmptyBorder(5,5,5,5)));
        keepLeftPnl.add(getMainPnl());
        insidePanel.add(keepLeftPnl, gbc);

        gbc.gridy++;

        JPanel workingDirPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        workingDirPnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(WORKING_DIRECTORY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        workingDirPnl.add(getWorkingDirPnl());
        insidePanel.add(workingDirPnl, gbc);

        add(insidePanel);
    }

    private JPanel getMainPnl() {
        if (mainPnl == null) {
            mainPnl = new JPanel(new GridBagLayout());

            JPanel upperPartPnl = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 2, 10, 2);
            upperPartPnl.add(new JLabel(UiHelper.translate("mmr.comServerInformation") + ":"), gbc);

            gbc.gridx++;
            upperPartPnl.add(getServerInfoLbl(), gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            upperPartPnl.add(new JLabel(UiHelper.translate("gettasks") + ":"), gbc);

            gbc.gridx++;
            gbc.gridwidth = 2;
            upperPartPnl.add(queryDatePnl, gbc);

            gbc.gridx += 2;
            gbc.gridwidth = 1;
            upperPartPnl.add(getGetTasksBtn(), gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(2, 2, 2, 2);
            upperPartPnl.add(new JLabel(UiHelper.translate("database") + ":"), gbc);

            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JPanel btnPnl = new JPanel(new GridLayout(1, 0, 10, 3));
            btnPnl.add(getConnectBtn());
            btnPnl.add(getStoreDataBtn());
            upperPartPnl.add(btnPnl, gbc);

            GridBagConstraints gbc_main = new GridBagConstraints();
            gbc_main.gridx = 0;
            gbc_main.gridy = 0;
            gbc_main.anchor = GridBagConstraints.WEST;
            gbc_main.insets = new Insets(5, 5, 5, 5);
            mainPnl.add(upperPartPnl, gbc_main);
        }
        return mainPnl;
    }

    private JPanel getWorkingDirPnl() {
        if (workingDirPnl == null) {
            workingDirPnl = new JPanel(new GridBagLayout());

            JPanel upperPartPnl = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 2, 10, 2);
            upperPartPnl.add(new JLabel(WORKING_DIRECTORY_DATA), gbc);

            gbc.gridx++;
            upperPartPnl.add(getWorkingDirDataFld(), gbc);

            gbc.gridx++;
            upperPartPnl.add(getWorkingDirDataBtn(), gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            upperPartPnl.add(new JLabel(WORKING_DIRECTORY_SYSTEM), gbc);

            gbc.gridx++;
            upperPartPnl.add(getWorkingDirSystemFld(), gbc);

            gbc.gridx++;
            upperPartPnl.add(getWorkingDirSystemBtn(), gbc);
            GridBagConstraints gbc_main = new GridBagConstraints();
            gbc_main.gridx = 0;
            gbc_main.gridy = 0;
            gbc_main.anchor = GridBagConstraints.WEST;
            gbc_main.insets = new Insets(5, 5, 5, 5);
            workingDirPnl.add(upperPartPnl, gbc_main);
        }
        return workingDirPnl;
    }

    private JTextField getWorkingDirDataFld() {
        if (workingDirDataFld == null) {
            workingDirDataFld = new JTextField(RegistryConfiguration.getDefault().get(TEMP_DATA_FILES_DIRECTORY));
            workingDirDataFld.setEditable(false);
            workingDirDataFld.setPreferredSize(new Dimension(WORKING_DIRECTORY_FLD_WIDTH, WORKING_DIRECTORY_FLD_HEIGHT));
        }
        return workingDirDataFld;
    }

    private JTextField getWorkingDirSystemFld() {
        if (workingDirSystemFld == null) {
            workingDirSystemFld = new JTextField(RegistryConfiguration.getDefault().get(TEMP_SYSTEM_FILES_DIRECTORY));
            workingDirSystemFld.setEditable(false);
            workingDirSystemFld.setPreferredSize(new Dimension(WORKING_DIRECTORY_FLD_WIDTH, WORKING_DIRECTORY_FLD_HEIGHT));
        }
        return workingDirSystemFld;
    }

    private JButton getWorkingDirDataBtn() {
        if (workingDirDataBtn == null) {
            workingDirDataBtn = new JButton(CHANGE_WORKING_DIRECTORY);
            workingDirDataBtn.setMnemonic(KeyEvent.VK_U);
            workingDirDataBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    doChangeWorkingDir(TEMP_DATA_FILES_DIRECTORY, workingDirDataFld);
                }
            });
        }
        return workingDirDataBtn;
    }

    private JButton getWorkingDirSystemBtn() {
        if (workingDirSystemBtn == null) {
            workingDirSystemBtn = new JButton(CHANGE_WORKING_DIRECTORY);
            workingDirSystemBtn.setMnemonic(KeyEvent.VK_U);
            workingDirSystemBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    doChangeWorkingDir(TEMP_SYSTEM_FILES_DIRECTORY, workingDirSystemFld);
                }
            });
        }
        return workingDirSystemBtn;
    }

    private void doChangeWorkingDir(String currentDirectory, JTextField currentTextField) {
        getWorkingDirFileChr().setCurrentDirectory(new File(RegistryConfiguration.getDefault().get(currentDirectory)));
        int result = getWorkingDirFileChr().showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String input = getWorkingDirFileChr().getSelectedFile().getAbsolutePath();
            if (input != null) {
                RegistryConfiguration.getDefault().set(currentDirectory, input);
                currentTextField.setText(input);
                JOptionPane.showMessageDialog(this, CHANGE_WORKING_DIRECTORY_RESTART_INFO, CHANGE_WORKING_DIRECTORY_RESTART_TITLE, JOptionPane.WARNING_MESSAGE);
            }

        }
    }

    private JFileChooser getWorkingDirFileChr () {
        if (workingDirFileChr == null) {
            workingDirFileChr =  new JFileChooser();
            workingDirFileChr.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        return workingDirFileChr;
    }

    public Date getQueryDate() {
        return queryDate.getQueryDate();
    }

    private JButton getGetTasksBtn() {
        if (getTasksBtn == null) {
            getTasksBtn = new JButton(UiHelper.translate("getTasks"));
            getTasksBtn.setMnemonic(KeyEvent.VK_G);
            getTasksBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    queryPendingComTasks();
                }
            });
        }
        return getTasksBtn;
    }

    private JButton getConnectBtn() {
        if (connectBtn == null) {
            connectBtn = new JButton(isConnected ? DISCONNECT : CONNECT);
            connectBtn.setMnemonic(KeyEvent.VK_C);
            connectBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    onConnectBtnPressed();
                }
            });
        }
        return connectBtn;
    }

    private JButton getStoreDataBtn() {
        if (storeDataBtn == null) {
            storeDataBtn = new JButton(STORE_DATA);
            storeDataBtn.setMnemonic(KeyEvent.VK_U);
            storeDataBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    doStoreData();
                }
            });
        }
        return storeDataBtn;
    }

    public void doStoreData() {
        try {
            storeCollectedData();
        } finally {
            mainFrame.getOfflineWorker().getOfflineExecuter().setStoringEndedClean(true);  //Reset store state
        }
    }

    private void queryPendingComTasks() {
        try {
            int retval = JOptionPane.OK_OPTION;
            if (mainFrame.getOfflineWorker().getTaskManager().hasCollectedData()) {
                retval = JOptionPane.showConfirmDialog(null,
                        UiHelper.translate("isTasksWaitingForStore"),
                        UiHelper.translate("warning"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
            }
            if (retval == JOptionPane.YES_OPTION) {
                //In this case, reset the comjobs from busy to pending so they can be picked up again
                mainFrame.getOfflineWorker().coldBoot(isConnected);
                queryTasksAndBuildTable();
            }
        } catch (DataAccessException e) {
            mainFrame.handleConnectionProblem(e);
        } catch (SyncException | IOException | ComServerRuntimeException | ProtocolRuntimeException e) {
            //If something else went wrong while sync'ing, show the error here
            JOptionPane.showConfirmDialog(null,
                    e.getMessage(),
                    UiHelper.translate("error"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void queryTasksAndBuildTable() {
        //The working thread queries the comjobs in the background.
        mainFrame.getOfflineWorker().getOfflineFrame().getOfflineExecuter().addAction(OfflineActions.QueryPendingComJobs);

        //Show a blocking progress bar. Thread continues when the working thread closes the progress bar.
        if (mainFrame.getOfflineWorker().getTaskManager().isOnline()) {
            showProgressBar(0, UiHelper.translate("mmr.gettingPendingTasks"), false);
        } else {
            showProgressBar(0, UiHelper.translate("mmr.loadingTasksFromFiles"), false);
        }
    }

    private void showProgressBar(final int size, final String message, boolean cancel) {
        indeterminateProgress = new IndeterminateProgress(size, mainFrame, this, message, cancel);
        indeterminateProgress.setVisible(true);
    }

    public void cancel() {
        if (indeterminateProgress.getMessage().equals(STORING_COLLECTED_DATA_TITLE)) {

            //Add the action again to abort the ongoing storing
            mainFrame.getOfflineWorker().getOfflineExecuter().addAction(OfflineActions.StoreCollectedData);

            //Now wait until the current model was fully stored (or timeout 120 sec)
            long timeoutMoment = System.currentTimeMillis() + 120000;
            while (!storingWasCanceled.get()) {
                if (System.currentTimeMillis() < timeoutMoment) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    mainFrame.getOfflineWorker().getOfflineExecuter().getLogging().log(Level.SEVERE, "Could not interrupt storing process properly, still busy after 120 seconds... Moving on.");
                    break;
                }
            }
        }
        indeterminateProgress = null;
    }

    private void onConnectBtnPressed() {
        try {
            toggleConnectionState();
            toggleControls();
            mainFrame.getOfflineWorker().warmBoot(isConnected);
            if (isConnected) {
                storeCollectedData();   //Blocking method until all data is stored
                if (!storingWasCanceled.get()) {
                    if (mainFrame.getOfflineWorker().getOfflineExecuter().isStoringEndedClean()) {
                        queryPendingComTasks(); //After storing, start reading the new pending comjobs
                    }
                }
                mainFrame.refreshCompletionCodes();
            }
        } catch (DataAccessException e) {
            mainFrame.handleConnectionProblem(e);
        } catch (SyncException | IOException | ComServerRuntimeException | ProtocolRuntimeException | ApplicationException e) {
            //If something else went wrong while sync'ing, set as disconnected and show the error here
            setConnected(false);
            toggleControls();
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), UiHelper.translate("error"), JOptionPane.ERROR_MESSAGE);

        } finally {
            mainFrame.getOfflineWorker().getOfflineExecuter().setStoringEndedClean(true);  //Reset store state
        }
    }

    public void setConnected(boolean newValue) {
        isConnected = newValue;
        mainFrame.setOnline(newValue);
        mainFrame.setCanStoreData(canStoreData());
        getConnectBtn().setText(isConnected ? DISCONNECT : CONNECT);
    }

    public boolean canStoreData() {
        return isConnected && mainFrame.getOfflineWorker().getTaskManager().needToStoreCollectedData();
    }

    private void toggleConnectionState() {
        setConnected(!isConnected);
    }

    public void toggleControls() {
        getGetTasksBtn().setEnabled(isConnected);
        getStoreDataBtn().setEnabled(canStoreData());
    }

    public void update() {
        getConnectBtn().setEnabled(true);
        getServerInfoLbl().setText(" " + mainFrame.getOfflineWorker().getComServerInfo());
        setConnected(mainFrame.getOfflineWorker().getTaskManager().isOnline());
        getStoreDataBtn().setEnabled(canStoreData());
    }

    private JLabel getServerInfoLbl() {
        if (serverInfoLbl == null) {
            serverInfoLbl = new JLabel();
        }
        return serverInfoLbl;
    }

    private void storeCollectedData() {
        storingWasCanceled.set(false);
        if (mainFrame.getOfflineWorker().getTaskManager().needToStoreCollectedData()) {
            //Trigger the action on the working thread
            mainFrame.getOfflineWorker().getOfflineExecuter().addAction(OfflineActions.StoreCollectedData);

            updateStoreDataButton(false);

            //Show an indefinite progress bar. This is blocking until all data is stored!
            //The storing thread will close the update the progress bar.
            showProgressBar(0, STORING_COLLECTED_DATA_TITLE, true);
        }
    }

    public void updateStoreDataButton(boolean storeState) {
        getStoreDataBtn().setText(storeState ? STORE_DATA : ABORT_STORE_DATA);
        if (storeState) {
            disableProgressBar();
        }
    }

    public void disableProgressBar() {
        if (SwingUtilities.isEventDispatchThread()) {
            closeProgressBar();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressBar();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                ;//Absorb
            }
        }
    }

    private void closeProgressBar() {
        if (indeterminateProgress != null) {
            indeterminateProgress.close();
            indeterminateProgress = null;
        }
    }

    public void setProgressBarSize(final int size) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (indeterminateProgress != null) {
                    indeterminateProgress.setSize(size);
                }
            }
        });
    }

    public void increaseProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (indeterminateProgress != null) {
                    indeterminateProgress.increaseProgress();
                }
            }
        });
    }

    public void setStoringWasCanceled(boolean storingWasCanceled) {
        this.storingWasCanceled.set(storingWasCanceled);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (OfflineFrame.PROPERTY_CANSTOREDATA.equals(evt.getPropertyName())) {
            getStoreDataBtn().setEnabled((boolean) evt.getNewValue());
        }
    }
}
