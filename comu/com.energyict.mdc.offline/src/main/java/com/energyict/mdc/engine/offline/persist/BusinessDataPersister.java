/*
 * BusinessObjectPersister.java
 *
 * Created on 19 november 2003, 8:58
 */

package com.energyict.mdc.engine.offline.persist;

import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.users.OfflineUserInfo;

import java.util.List;

/**
 * Class that allows to save certain business objects (e.g. OfflineComServerImpl) to txt files (using JSON serializing)
 * and also load these objects again, from the same txt files.
 * <p/>
 * Note that de/serializing an object can throw an ApplicationException if something goes wrong.
 * This exception is unhandled and will be shown (including stack trace) in the UI.
 */
public interface BusinessDataPersister {

    public OfflineComServer loadComServer();

    public List<OfflineUserInfo> loadUserInfos();

    public void saveUserInfos(List<OfflineUserInfo> userInfos);

    public List<LookupEntry> loadCompletionCodes();

    public void saveCompletionCodes(List<LookupEntry> codes);

    public void saveComServer(OfflineComServer offlineComServer);

    public void saveComJobModels(List<ComJobExecutionModel> comJobModels);

    public List<ComJobExecutionModel> loadComJobExecutionModels();

    public void saveComJobExecutionModel(ComJobExecutionModel comJobExecutionModel);

    public String getDataDir();

    public String getSystemDir();

}
