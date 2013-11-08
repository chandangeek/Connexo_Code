package com.energyict.protocolimplv2.ace4000.requests.tracking;

/**
 * Indicates the state of a certain request.
 * It can be sent (no response yet), it can be acked (success) or it can be nacked/rejected (fail).
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/01/13
 * Time: 11:09
 * Author: khe
 */
public enum RequestState {

    Sent(null),
    Success(true),
    Fail(false);

    private Boolean success;

    RequestState(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }
}