/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.common;

import com.energyict.obis.ObisCode;

/**
 * @author khe
 * @since 04.08.17 - 16:40
 */
public class IdAndSecuritySetupObisCode {

    private int clientToChangeKeyFor;
    private ObisCode clientSecuritySetupObis;

    public IdAndSecuritySetupObisCode(int clientToChangeKeyFor, ObisCode clientSecuritySetupObis) {
        this.clientToChangeKeyFor = clientToChangeKeyFor;
        this.clientSecuritySetupObis = clientSecuritySetupObis;
    }

    public int getClientToChangeKeyFor() {
        return clientToChangeKeyFor;
    }

    public void setClientToChangeKeyFor(int clientToChangeKeyFor) {
        this.clientToChangeKeyFor = clientToChangeKeyFor;
    }

    public ObisCode getClientSecuritySetupObis() {
        return clientSecuritySetupObis;
    }

    public void setClientSecuritySetupObis(ObisCode clientSecuritySetupObis) {
        this.clientSecuritySetupObis = clientSecuritySetupObis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdAndSecuritySetupObisCode that = (IdAndSecuritySetupObisCode) o;

        if (clientToChangeKeyFor != that.clientToChangeKeyFor) {
            return false;
        }
        return clientSecuritySetupObis.equals(that.clientSecuritySetupObis);
    }

    @Override
    public int hashCode() {
        int result = clientToChangeKeyFor;
        result = 31 * result + clientSecuritySetupObis.hashCode();
        return result;
    }
}