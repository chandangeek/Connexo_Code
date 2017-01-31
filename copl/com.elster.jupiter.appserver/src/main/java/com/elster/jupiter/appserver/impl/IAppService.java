/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Registration;

interface IAppService extends AppService {

    DataModel getDataModel();

    void stopAppServer();

    void startAsAppServer(String name);

    Registration addCommandListener(CommandListener commandListener);

    Thesaurus getThesaurus();

    interface CommandListener {

        void notify(AppServerCommand command);
    }
}
