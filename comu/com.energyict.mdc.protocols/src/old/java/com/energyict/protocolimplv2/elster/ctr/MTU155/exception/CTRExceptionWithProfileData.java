/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;


import com.energyict.mdc.protocol.api.device.data.ProfileData;

public class CTRExceptionWithProfileData extends CTRException {

    private final ProfileData profileData;
    private final Exception exception;

    public CTRExceptionWithProfileData(Exception e, ProfileData profileData) {
        super(e);
        this.profileData = profileData;
        this.exception = e;
    }

    public CTRExceptionWithProfileData(String message, Exception e, ProfileData profileData) {
        super(message, e);
        this.profileData = profileData;
        this.exception = e;
    }

    public ProfileData getProfileData() {
        return profileData;
    }

    public Exception getException() {
        return exception;
    }
}
