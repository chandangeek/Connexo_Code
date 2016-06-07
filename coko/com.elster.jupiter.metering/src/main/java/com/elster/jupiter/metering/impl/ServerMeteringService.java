package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.time.Clock;

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

    Clock getClock();

    EndDeviceEventTypeImpl createEndDeviceEventType(String mRID);

    EndDeviceControlTypeImpl createEndDeviceControlType(String mRID);

    void copyKeyIfMissing(NlsKey name, String localKey);
}