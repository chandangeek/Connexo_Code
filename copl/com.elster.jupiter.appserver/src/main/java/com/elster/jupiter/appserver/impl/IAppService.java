package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.orm.DataModel;

interface IAppService extends AppService {

    DataModel getDataModel();

    void stopAppServer();

    void startAsAppServer(String name);
}
