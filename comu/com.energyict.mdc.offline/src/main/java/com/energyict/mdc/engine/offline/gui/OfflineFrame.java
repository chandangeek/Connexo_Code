package com.energyict.mdc.engine.offline.gui;

import com.energyict.mdc.common.ComServerExecutionException;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.OfflineEngine;
import com.energyict.mdc.engine.offline.OfflineExecuter;
import com.energyict.mdc.engine.offline.core.OfflinePropertiesProvider;
import com.energyict.mdc.engine.offline.core.OfflineWorker;
import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.actions.ExitAction;
import com.energyict.mdc.engine.offline.gui.actions.ShowAboutAction;
import com.energyict.mdc.engine.offline.gui.decorators.EventDecorator;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.engine.offline.gui.dialogs.LoggingExceptionDialog;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.energyict.mdc.engine.offline.gui.windows.*;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The main class of the ComServer offline.
 * This GUI also launches the back end at start up.
 *
 * @author Koen, Geert (2014)
 */
public class OfflineFrame extends JFrame {

    private static final int DEFAULT_FILTER_FIELD_SIZE = 12;

    private JPanel mainPnl;
    private JideTabbedPane tabbedPane;
    private ConfigPnl configPnl;
    private JideTabbedPane tasksPane;
    private JPanel tasksPnl;
    private JideTabbedPane loggingPane;
    private CommunicationLoggingPanel communicationLoggingPnl;
    private JPanel filterPnl;
    private JLabel nameLbl;
    private JTextField nameField;
    private JButton clearNameFilterBtn;
    private JLabel serialLbl;
    private JTextField serialField;
    private JButton clearSerialFilterBtn;
    private JLabel locationLbl;
    private JTextField locationField;
    private JButton clearLocationFilterBtn;
    private JLabel usagePointLbl;
    private JTextField usagePointField;
    private JButton clearUsagePointFilterBtn;
    private JLabel onlineLbl;
    private JButton aboutBtn;
    private JButton exitBtn;


    private final static MouseAdapter mouseAdapter = new MouseAdapter() {
    };

    private static final int iDefaultWidth = 1000;
    private static final int iDefaultHeight = 600;
    private static final ImageIcon mainIcon = (ImageIcon) EisIcons.EISERVER_ICON;

    private final TaskManagementPanel taskManagementPanel;
    private final TaskExecutionPanel taskExecutionPanel;
    private final OfflineWorker offlineWorker;

    private OfflineExecuter offlineExecuter = null;
    private RunningComServerImpl.ServiceProvider serviceProvider;
    private String nameMask;
    private String serialMask;
    private String locationMask;
    private String usagePointMask;
    private List<LookupEntry> completionCodes;

    private boolean canStoreData = false;
    private boolean online = false;
    private PropertyChangeSupport propertyChangeSupport;
    public static String PROPERTY_CANSTOREDATA = "canStoreData";

    public OfflineFrame(RunningComServerImpl.ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        offlineWorker = new OfflineWorker(this);
        taskManagementPanel = new TaskManagementPanel(offlineWorker);
        taskExecutionPanel = new TaskExecutionPanel(offlineWorker);
        initComponents();
        initExtra();
        invokeUpdateConfigPanel();
    }

    public void startOfflineExecuter() {
        getOfflineExecuter().init();
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null && defaultUncaughtExceptionHandler instanceof LoggingExceptionDialog) {
            ((LoggingExceptionDialog) defaultUncaughtExceptionHandler).setOfflineExecuter(getOfflineExecuter());
        }

        getOfflineExecuter().process();
    }

    private void initExtra() {
        // general stuff
        setSize(iDefaultWidth, iDefaultHeight);
        java.util.List<Image> iconImages = new ArrayList<>();
        iconImages.add(mainIcon.getImage());
        iconImages.add(((ImageIcon) EisIcons.EISERVER_ICON_32).getImage());
        setIconImages(iconImages);
        //initializeCompletionCodes();
    }

    /**
     * Stop the running comserver before closing the application
     */
    public void doClose() {
        if (getOfflineWorker().getOfflineExecuter().comServerIsStarted()) {
            getOfflineWorker().getOfflineExecuter().getRunningComServer().shutdownImmediate();
        }
        this.dispose();
    }

    public void startWaitCursor() {
        getGlassPane().addMouseListener(mouseAdapter);
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
    }

    public void stopWaitCursor() {
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        getGlassPane().removeMouseListener(mouseAdapter);
        getGlassPane().setVisible(false);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        String version = OfflinePropertiesProvider.getInstance().getConnexoVersion();
        setTitle(TranslatorProvider.instance.get().getTranslator().getTranslation("commserveroffline") + " " + version);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                OfflineEngine.exitSystem(0);
            }
        });

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(getMainPnl(), BorderLayout.CENTER);
        pack();
    }

    public ImageIcon getMainIcon() {
        return mainIcon;
    }

    private JPanel getMainPnl() {
        if (mainPnl == null) {
            mainPnl = new JPanel(new BorderLayout(5, 5));
            mainPnl.add(getTabbedPane(), BorderLayout.CENTER);
        }
        return mainPnl;
    }

    private JideTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JideTabbedPane();
            tabbedPane.add("   " + UiHelper.translate("configuration") + "   ", getConfigPnl());
            tabbedPane.add("   " + UiHelper.translate("tasks") + "   ", getTasksPnl());
            tabbedPane.add("   " + UiHelper.translate("logging") + "   ", getLoggingPane());
            tabbedPane.setIconAt(0, MdwIcons.SPACER_1x32_ICON);
            tabbedPane.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    int index = tabbedPane.getSelectedIndex();
                    if (index == 1) { // 'Tasks' selected
                        onTasksTabChange();
                    }
                }
            });
            JPanel onlinePnl = new JPanel(new BorderLayout(3, 3));
            onlinePnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            JPanel buttonPnl = new JPanel(new GridLayout(1, 0, 5, 5));
            buttonPnl.add(getAboutBtn());
            buttonPnl.add(getExitBtn());

            JPanel trailingPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            trailingPnl.add(buttonPnl);
            trailingPnl.add(getOnlineLbl());
            onlinePnl.add(trailingPnl, BorderLayout.CENTER);
            tabbedPane.setTabTrailingComponent(onlinePnl);
        }
        return tabbedPane;
    }

    private ConfigPnl getConfigPnl() {
        if (configPnl == null) {
            configPnl = new ConfigPnl(this);
        }
        return configPnl;
    }

    /**
     * In case of any problem with the websocket connection to the online comserver
     * Do the proper UI actions, show the error message to the user
     */
    public void handleConnectionProblem(DataAccessException e) {
        getConfigPnl().setConnected(false);
        getConfigPnl().toggleControls();
        getOfflineWorker().getTaskManager().setOnline(false);
        logAndShowProblem(e);
    }

    public void handleRuntimeProblem(ComServerExecutionException e) {
        logAndShowProblem(e);
    }

    protected void logAndShowProblem(ComServerExecutionException e) {
        if (getOfflineExecuter().getLogging() != null) {
            getOfflineExecuter().getLogging().getLogger().severe(e.toString() + ", " + Utils.stack2string(e));
        }
        getOfflineExecuter().stopRemoteComServerDAO(false);
        JOptionPane.showMessageDialog(this, e.getMessage(), UiHelper.translate("error"), JOptionPane.ERROR_MESSAGE);
    }

    private JPanel getTasksPnl() {
        if (tasksPnl == null) {
            tasksPnl = new JPanel(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(getFilterPnl());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            tasksPnl.add(scrollPane, BorderLayout.NORTH);
            tasksPnl.add(getTasksPane(), BorderLayout.CENTER);
        }
        return tasksPnl;
    }

    private JPanel getFilterPnl() {
        if (filterPnl == null) {
            double neededWidth = getNameLbl().getPreferredSize().getWidth() + getNameField().getPreferredSize().getWidth() + getClearNameFilterBtn().getPreferredSize().getWidth() +
                    getSerialLbl().getPreferredSize().getWidth() + getSerialField().getPreferredSize().getWidth() + getClearSerialFilterBtn().getPreferredSize().getWidth() +
                    getLocationLbl().getPreferredSize().getWidth() + getLocationField().getPreferredSize().getWidth() + getClearLocationFilterBtn().getPreferredSize().getWidth() +
                    getUsagePointLbl().getPreferredSize().getWidth() + getUsagePointField().getPreferredSize().getWidth() + getClearUsagePointFilterBtn().getPreferredSize().getWidth() +
                    50; // Reserve some space for margins & borders

            if (neededWidth <  GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth()) {
                // Then opt for a flow layout
                filterPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
                filterPnl.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("  " + UiHelper.translate("filter") + "  "),
                                new EmptyBorder(0, 0, 10, 0)     // Reserve some place for the horizontal scroll bare
                        )
                );

                filterPnl.add(getNameLbl());
                filterPnl.add(getNameField());
                filterPnl.add(getClearNameFilterBtn());
                filterPnl.add(getSerialLbl());
                filterPnl.add(getSerialField());
                filterPnl.add(getClearSerialFilterBtn());
                filterPnl.add(getLocationLbl());
                filterPnl.add(getLocationField());
                filterPnl.add(getClearLocationFilterBtn());
                filterPnl.add(getUsagePointLbl());
                filterPnl.add(getUsagePointField());
                filterPnl.add(getClearUsagePointFilterBtn());
            } else {
                // Else opt for a GridBagLayout and split content over 2 rows
                int x = 0; int y = 0;
                JPanel innerPnl = new JPanel(new GridBagLayout());
                innerPnl.add(getNameLbl(), UiHelper.createGbc(x++, y));
                innerPnl.add(getNameField(), UiHelper.createGbc(x++, y));
                innerPnl.add(getClearNameFilterBtn(), UiHelper.createGbc(x++, y));
                innerPnl.add(getSerialLbl(), UiHelper.createGbc(x++, y));
                innerPnl.add(getSerialField(), UiHelper.createGbc(x++, y));
                innerPnl.add(getClearSerialFilterBtn(), UiHelper.createGbc(x++, y++));
                x = 0;
                innerPnl.add(getLocationLbl(), UiHelper.createGbc(x++, y));
                innerPnl.add(getLocationField(), UiHelper.createGbc(x++, y));
                innerPnl.add(getClearLocationFilterBtn(), UiHelper.createGbc(x++, y));
                innerPnl.add(getUsagePointLbl(), UiHelper.createGbc(x++, y));
                innerPnl.add(getUsagePointField(), UiHelper.createGbc(x++, y));
                innerPnl.add(getClearUsagePointFilterBtn(), UiHelper.createGbc(x++, y));

                filterPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
                filterPnl.setBorder(BorderFactory.createTitledBorder("  " + UiHelper.translate("filter") + "  "));
                filterPnl.add(innerPnl);
            }
        }
        return filterPnl;
    }

    private JLabel getNameLbl() {
        if (nameLbl == null) {
            nameLbl = new JLabel(UiHelper.translate("rtuName") + ":");
            nameLbl.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
        }
        return nameLbl;
    }

    private JTextField getNameField() {
        if (nameField == null) {
            nameField = new JTextField(DEFAULT_FILTER_FIELD_SIZE);
            nameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateFilter();
                }
            });
        }
        return nameField;
    }

    private JButton getClearNameFilterBtn() {
        if (clearNameFilterBtn == null) {
            clearNameFilterBtn = new JButton();
            clearNameFilterBtn.setIcon(MdwIcons.DELETE_ICON);
            clearNameFilterBtn.setDisabledIcon(MdwIcons.createGrayedOut(MdwIcons.DELETE_ICON));
            clearNameFilterBtn.setToolTipText(UiHelper.translate("clearFilter"));
            clearNameFilterBtn.setEnabled(false);
            clearNameFilterBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getNameField().setText(null);
                    getNameField().requestFocus();
                    updateFilter();
                }
            });
        }
        return clearNameFilterBtn;
    }

    private JLabel getSerialLbl() {
        if (serialLbl == null) {
            serialLbl = new JLabel(UiHelper.translate("rtuSerialNr") + ":");
            serialLbl.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
        }
        return serialLbl;
    }

    private JTextField getSerialField() {
        if (serialField == null) {
            serialField = new JTextField(DEFAULT_FILTER_FIELD_SIZE);
            serialField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateFilter();
                }
            });
        }
        return serialField;
    }

    private JButton getClearSerialFilterBtn() {
        if (clearSerialFilterBtn == null) {
            clearSerialFilterBtn = new JButton();
            clearSerialFilterBtn.setIcon(MdwIcons.DELETE_ICON);
            clearSerialFilterBtn.setDisabledIcon(MdwIcons.createGrayedOut(MdwIcons.DELETE_ICON));
            clearSerialFilterBtn.setToolTipText(UiHelper.translate("clearFilter"));
            clearSerialFilterBtn.setEnabled(false);
            clearSerialFilterBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getSerialField().setText(null);
                    getSerialField().requestFocus();
                    updateFilter();
                }
            });
        }
        return clearSerialFilterBtn;
    }

    private JLabel getLocationLbl() {
        if (locationLbl == null) {
            locationLbl = new JLabel(UiHelper.translate("location") + ":");
            locationLbl.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
        }
        return locationLbl;
    }

    private JTextField getLocationField() {
        if (locationField == null) {
            locationField = new JTextField(DEFAULT_FILTER_FIELD_SIZE);
            locationField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateFilter();
                }
            });
        }
        return locationField;
    }

    private JButton getClearLocationFilterBtn() {
        if (clearLocationFilterBtn == null) {
            clearLocationFilterBtn = new JButton();
            clearLocationFilterBtn.setIcon(MdwIcons.DELETE_ICON);
            clearLocationFilterBtn.setDisabledIcon(MdwIcons.createGrayedOut(MdwIcons.DELETE_ICON));
            clearLocationFilterBtn.setToolTipText(UiHelper.translate("clearFilter"));
            clearLocationFilterBtn.setEnabled(false);
            clearLocationFilterBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getLocationField().setText(null);
                    getLocationField().requestFocus();
                    updateFilter();
                }
            });
        }
        return clearLocationFilterBtn;
    }

    private JLabel getUsagePointLbl() {
        if (usagePointLbl == null) {
            usagePointLbl = new JLabel(UiHelper.translate("usagePoint") + ":");
            usagePointLbl.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
        }
        return usagePointLbl;
    }

    private JTextField getUsagePointField() {
        if (usagePointField == null) {
            usagePointField = new JTextField(DEFAULT_FILTER_FIELD_SIZE);
            usagePointField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateFilter();
                }
            });
        }
        return usagePointField;
    }

    private JButton getClearUsagePointFilterBtn() {
        if (clearUsagePointFilterBtn == null) {
            clearUsagePointFilterBtn = new JButton();
            clearUsagePointFilterBtn.setIcon(MdwIcons.DELETE_ICON);
            clearUsagePointFilterBtn.setDisabledIcon(MdwIcons.createGrayedOut(MdwIcons.DELETE_ICON));
            clearUsagePointFilterBtn.setToolTipText(UiHelper.translate("clearFilter"));
            clearUsagePointFilterBtn.setEnabled(false);
            clearUsagePointFilterBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getUsagePointField().setText(null);
                    getUsagePointField().requestFocus();
                    updateFilter();
                }
            });
        }
        return clearUsagePointFilterBtn;
    }

    private JideTabbedPane getTasksPane() {
        if (tasksPane == null) {
            tasksPane = new JideTabbedPane();
            tasksPane.add("   " + UiHelper.translate("mmr.todo") + "   ", getTaskExecutionPanel());
            tasksPane.add("   " + UiHelper.translate("done") + "   ", getTaskManagementPanel());
            tasksPane.setIconAt(0, MdwIcons.SPACER_1x32_ICON);
            tasksPane.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    onTasksTabChange();
                }
            });
        }
        return tasksPane;
    }

    private void onTasksTabChange() {
        invokeUpdateConfigPanel();
    }

    private JideTabbedPane getLoggingPane() {
        if (loggingPane == null) {
            loggingPane = new JideTabbedPane();
            TransactionLoggingDialog transactionLoggingDialog = new TransactionLoggingDialog(this, "", new ComServerMobileDialogSettings("transactionslogdialog", 700, 350));
            loggingPane.add("   " + UiHelper.translate("transaction") + "   ", transactionLoggingDialog.getContentPane());
            loggingPane.add("   " + UiHelper.translate("protocol") + "   ", getCommunicationLoggingPnl());
            loggingPane.setIconAt(0, MdwIcons.SPACER_1x32_ICON);
        }
        return loggingPane;
    }

    public OfflineExecuter getOfflineExecuter() {
        if (offlineExecuter == null) {
            offlineExecuter = new OfflineExecuter(offlineWorker.getFileManager(), serviceProvider);
        }
        return offlineExecuter;
    }

    public void comServerMobileStarted() {
        getConfigPnl().queryTasksAndBuildTable();
        invokeUpdateConfigPanel();
        super.validate();
        if (!offlineExecuter.isComServerObjectAvailable()) {
            JOptionPane.showMessageDialog(this,
                    TranslatorProvider.instance.get().getTranslator().getTranslation("commservernotready"),
                    TranslatorProvider.instance.get().getTranslator().getTranslation("message"),
                    JOptionPane.WARNING_MESSAGE);
        }

        setWaitingCursor(false);
    }

    public OfflineWorker getOfflineWorker() {
        return offlineWorker;
    }

    public void setWaitingCursor(final boolean busy) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (busy) {
                startWaitCursor();
            } else {
                stopWaitCursor();
            }
        } else {
            Runnable doUpdate = new Runnable() {
                public void run() {
                    if (busy) {
                        startWaitCursor();
                    } else {
                        stopWaitCursor();
                    }
                }
            };
            SwingUtilities.invokeLater(doUpdate);
        }
    }

    public void invokeUpdateConfigPanel() {
        if (SwingUtilities.isEventDispatchThread()) {
            updateConfigPanel();
        } else {
            Runnable doUpdate = new Runnable() {
                public void run() {
                    updateConfigPanel();
                }
            };
            SwingUtilities.invokeLater(doUpdate);
        }
    }

    private void updateConfigPanel() {
        getConfigPnl().update();
    }

    public void updateTasks(final ComJobExecutionModel model) {
        if (model != null) {
            getOfflineWorker().getTaskManager().updateTaskRows(model);
        }
        getTaskExecutionPanel().initializeRows();
        getTaskManagementPanel().initializeRows();
    }

    public TaskExecutionPanel getTaskExecutionPanel() {
        return taskExecutionPanel;
    }

    public TaskManagementPanel getTaskManagementPanel() {
        return taskManagementPanel;
    }

    public void notifyOfComServerMonitorEvent(final EventDecorator event) {
        if (EventQueue.isDispatchThread()) {
            getCommunicationLoggingPnl().notifyOfComServerMonitorEvent(event);
        } else {
            Runnable doUpdate = new Runnable() {
                public void run() {
                    getCommunicationLoggingPnl().notifyOfComServerMonitorEvent(event);
                }
            };
            EventQueue.invokeLater(doUpdate);
        }
    }

    public CommunicationLoggingPanel getCommunicationLoggingPnl() {
        if (this.communicationLoggingPnl == null) {
            this.communicationLoggingPnl = new CommunicationLoggingPanel(/*this*/);
        }
        return this.communicationLoggingPnl;
    }

    public Date getQueryDate() {
        return getConfigPnl().getQueryDate();
    }

    public void stopProgressBar() {
        getConfigPnl().disableProgressBar();
    }

    public void setProgressBarSize(final int size) {
        getConfigPnl().setProgressBarSize(size);
    }

    public void increaseProgress() {
        getConfigPnl().increaseProgress();
    }

    public void doStoreData() {
        getConfigPnl().doStoreData();
    }

    public void setStoringWasCanceled(boolean storingWasCanceled) {
        getConfigPnl().setStoringWasCanceled(storingWasCanceled);
    }

    public void endStoringProgress() {
        getConfigPnl().updateStoreDataButton(true);
        getTaskManagementPanel().updateUploadBtn(true);
    }

    public boolean isCanStoreData() {
        return canStoreData;
    }

    public void setCanStoreData(boolean newValue) {
        boolean oldValue = canStoreData;
        canStoreData = newValue;
        getPropertyChangeSupport().firePropertyChange(PROPERTY_CANSTOREDATA, oldValue, newValue);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean newValue) {
        online = newValue;
        updateOnlineLbl();
    }

    private JLabel getOnlineLbl() {
        if (onlineLbl == null) {
            onlineLbl = new JLabel();
        }
        return onlineLbl;
    }

    private void updateOnlineLbl() {
        getOnlineLbl().setIcon(isOnline() ? MdwIcons.ONLINE_ICON_24 : MdwIcons.OFFLINE_ICON_24);
        getOnlineLbl().setText(UiHelper.translate(isOnline() ? "goonline" : "offline"));
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

    public boolean passesThruFilter(ComJobExecutionModel model) {
        List<String> fields2Check = new ArrayList<>();
        fields2Check.add(model.getDevice().getName().toUpperCase());
        fields2Check.add(model.getDevice().getSerialNumber() != null ? model.getDevice().getSerialNumber().toUpperCase() : null);
        fields2Check.add(model.getOfflineDevice().getLocation() != null ? model.getOfflineDevice().getLocation().toUpperCase() : null);
        fields2Check.add(model.getOfflineDevice().getUsagePoint() != null ? model.getOfflineDevice().getUsagePoint().toUpperCase() : null);

        List<String> masks2Use = new ArrayList<>();
        masks2Use.add(nameMask);
        masks2Use.add(serialMask);
        masks2Use.add(locationMask);
        masks2Use.add(usagePointMask);

        for (int index = 0, max = fields2Check.size(); index < max; index++) {
            String field2Check = fields2Check.get(index);
            String mask2Use = masks2Use.get(index);

            if (field2Check == null) {
                if (mask2Use != null && mask2Use.trim().length() > 0) {
                    return false; // an empty field doesn't fit the mask
                } else {
                    continue; // an empty field fits an empty mask
                }
            }

            if (mask2Use == null || mask2Use.trim().length() == 0) {
                continue; // this field passes (since there's no mask to fit), so continue
            }
            if (field2Check.indexOf(mask2Use.toUpperCase()) == -1) {
                return false; // this field doesn't fit the mask
            }
            // this field passes, try the next one
        }
        return true; // all fields pass
    }

    private void updateFilter() {
        nameMask = getNameField().getText();
        serialMask = getSerialField().getText();
        locationMask = getLocationField().getText();
        usagePointMask = getUsagePointField().getText();
        getClearNameFilterBtn().setEnabled(nameMask != null && nameMask.trim().length() > 0);
        getClearSerialFilterBtn().setEnabled(serialMask != null && serialMask.trim().length() > 0);
        getClearLocationFilterBtn().setEnabled(locationMask != null && locationMask.trim().length() > 0);
        getClearUsagePointFilterBtn().setEnabled(usagePointMask != null && usagePointMask.trim().length() > 0);
        updateTasks(null);
    }

    private JButton getAboutBtn() {
        if (aboutBtn == null) {
            aboutBtn = new JButton(new ShowAboutAction(this));
        }
        return aboutBtn;
    }

    private JButton getExitBtn() {
        if (exitBtn == null) {
            exitBtn = new JButton(new ExitAction(this));
        }
        return exitBtn;
    }

    public void initializeCompletionCodes() {
        boolean online = false;
        ComServerDAO remoteComServerDAO = null;
        if (new RemoteProperties(OfflineComServerProperties.getInstance().getProperties()).getRemoteQueryApiUrl() != null) {
            try {
                remoteComServerDAO = getOfflineExecuter().getRemoteComServerDAO();
                online = true;
            } catch (Throwable e) {
                online = false;
            }
        }

        if (online) {
            try {
                completionCodes = remoteComServerDAO.getCompletionCodeLookupEntries();
            } catch (DataAccessException e) {
                handleConnectionProblem(e);
                return; //No codes for now, will be retried when the user goes online again
            }
            getOfflineWorker().getFileManager().saveCompletionCodes(completionCodes);
        } else {
            completionCodes = getOfflineWorker().getFileManager().loadCompletionCodes();
        }
    }

    public List<LookupEntry> getCompletionCodes() {
        if (completionCodes == null) {
            initializeCompletionCodes();
        }
        return completionCodes;
    }

    public void refreshCompletionCodes() {
        completionCodes = null;
        getCompletionCodes();
    }

    public void showDirectoryInitializationErrorDialog() {
        JOptionPane.showMessageDialog(this,
                UiHelper.translate("workingDirectoryCopyError"),
                UiHelper.translate("error"),
                JOptionPane.ERROR_MESSAGE);
    }

    public void showModalDialog(JPanel panel, String strTitle) {
        showModalDialog(panel, strTitle, new Dimension(0, 0));
    }

    public void showModalDialog(JPanel panel, String strTitle, Dimension dSize) {
        showModalDialog(panel, strTitle, dSize, new Dimension(0, 0));
    }

    public void showModalDialog(JPanel panel, String strTitle,
                                double percentageOfFullScreenWidth,
                                double percentageOfFullScreenHeight) {
        Dimension dFullScreen = this.getToolkit().getScreenSize();
        Dimension size = new Dimension();
        size.setSize(dFullScreen.getWidth() * percentageOfFullScreenWidth,
                dFullScreen.getHeight() * percentageOfFullScreenHeight);
        showModalDialog(panel, strTitle, size, dFullScreen);
    }

    public void showModalDialog(JPanel panel, String strTitle, Dimension dSize, Dimension dMaxSize) {
        showModalDialog(getModalDialog(panel, strTitle, dSize, dMaxSize));
    }

    public void showModalDialog(EisDialog dialog) {
        dialog.setVisible(true);
    }

    public EisDialog getModalDialog(JPanel panel, String strTitle, Dimension dSize, Dimension dMaxSize) {
        return getModalDialog(panel, strTitle, dSize, dMaxSize);
    }

    public boolean applyNewFontSize() {
        UiHelper.applyFontSize();
        SwingUtilities.updateComponentTreeUI(UiHelper.getMainWindow());
        return true; // font applied
    }
}
