package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;

/**
 * Add behavior to the {@link MeteringService}
 * that is reserved for server-side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-20 (10:17)
 */
public interface ServerMeteringService extends MeteringService {

    public DataModel getDataModel();

    public EndDeviceEventTypeImpl createEndDeviceEventType(String mRID);

}