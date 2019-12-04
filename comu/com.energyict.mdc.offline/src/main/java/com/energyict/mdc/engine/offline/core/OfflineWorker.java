/*
 * CommServerGuiJOfflineWorker.java
 *
 * Created on 30 september 2003, 16:29
 */

package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.offline.OfflineExecuter;
import com.energyict.mdc.engine.offline.core.exception.SyncException;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.table.TableCreator;
import com.energyict.mdc.engine.offline.persist.BusinessDataPersister;
import com.energyict.mdc.engine.offline.persist.FileManager;

import java.io.IOException;

/**
 * @author Koen
 */
public class OfflineWorker {

    private OfflineFrame offlineFrame;
    private TaskManager taskManager;
    private FileManager fileManager;

    /**
     * Creates a new instance of CommServerGuiJOfflineWorker
     */
    public OfflineWorker(OfflineFrame offlineFrame) {
        this.offlineFrame = offlineFrame;
        taskManager = new TaskManager(this);
        fileManager = new FileManager();
        if (fileManager.hasDirectoryInitializationError()) {
            offlineFrame.showDirectoryInitializationErrorDialog();
        }
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public BusinessDataPersister getFileManager() {
        return fileManager;
    }

    public String getComServerInfo() {
        String info = getOfflineExecuter().getComServerInfo();
        if (info == null) {
            return TranslatorProvider.instance.get().getTranslator().getTranslation("nobootinfo");
        } else {
            return info;
        }
    }

    public OfflineExecuter getOfflineExecuter() {
        return offlineFrame.getOfflineExecuter();
    }

    public OfflineFrame getOfflineFrame() {
        return offlineFrame;
    }

    public TableCreator getTaskExecutionTableCreatorAllTasks() {
        return getOfflineFrame().getTaskExecutionPanel().getTableCreatorAllTasks();
    }

//    public TableCreator getTaskExecutionTableCreatorFoundTasks() {
//        return getOfflineFrame().getTaskExecutionPanel().getTableCreatorFoundTasks();
//    }

    public TableCreator getTaskManagementTableCreator() {
        return getOfflineFrame().getTaskManagementPanel().getTableCreator();
    }

    /*
    *  warmBoot and initiate the bootprocess for the commserver application.
    *  @param online true to initiate bootprocess, false if offline
    */
    public void warmBoot(boolean online) throws SyncException, IOException, DataAccessException {
        try {
            if (online) {
                offlineFrame.startWaitCursor();
                getOfflineExecuter().warmBoot();
            } else {
                getOfflineExecuter().stopRemoteComServerDAO();  // In case of disconnect, then stop the RemoteComServerDAO
            }
            getTaskManager().setOnline(online);
            offlineFrame.invokeUpdateConfigPanel();
        } finally {
            offlineFrame.stopWaitCursor();
        }
    }

    /*
    *  ColdBoot and initiate the boot process for the comserver application.
    *  @param online true to initiate boot process, false if offline
    */
    public void coldBoot(boolean online) throws SyncException, IOException, DataAccessException {
        try {
            if (online) {
                offlineFrame.startWaitCursor();
                getOfflineExecuter().coldBoot();
            }
            getTaskManager().setOnline(online);
            offlineFrame.invokeUpdateConfigPanel();
        } finally {
            offlineFrame.stopWaitCursor();
        }
    }
}