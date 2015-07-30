package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

/**
 * Adds behavior to {@link MeteringService} that is specific
 * to server-side component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (14:56)
 */
public interface ServerMeteringService extends MeteringService {

    DataModel getDataModel();

    Thesaurus getThesaurus();

    EndDeviceEventTypeImpl createEndDeviceEventType(String mRID);

}