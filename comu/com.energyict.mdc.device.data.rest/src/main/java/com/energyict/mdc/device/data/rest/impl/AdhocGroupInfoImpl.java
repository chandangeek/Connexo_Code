package com.energyict.mdc.device.data.rest.impl;

/**
 * Created by Lucian on 1/12/2015.
 */

public class AdhocGroupInfoImpl implements com.energyict.mdc.device.data.rest.AdhocGroupInfo {
    public String name;

    public AdhocGroupInfoImpl() {
    }


    @Override
    public String getName() {
        return this.name;
    }
}
