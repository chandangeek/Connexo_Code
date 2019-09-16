package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;

public class WebSocketQueryApiServiceFactory {

    private static WebSocketQueryApiServiceFactory soleInstance;

    public static WebSocketQueryApiServiceFactory getInstance () {
        if (soleInstance == null) {
            soleInstance = new WebSocketQueryApiServiceFactory();
        }
        return soleInstance;
    }

    public static void setInstance (WebSocketQueryApiServiceFactory factory) {
        soleInstance = factory;
    }

    public WebSocketQueryApiService newWebSocketQueryApiService(RunningOnlineComServer comServer, ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService) {
        return new WebSocketQueryApiService(comServer, comServerDAO, engineConfigurationService ,connectionTaskService ,communicationTaskService, transactionService);
    }


    // Hide utility class constructor
    protected WebSocketQueryApiServiceFactory () {}

}
