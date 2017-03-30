/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfilesOfDeviceData', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Mdc.store.LoadProfileDataDurations'
    ],
    model: 'Mdc.model.LoadProfilesOfDeviceData',
    storeId: 'LoadProfilesOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/loadprofiles/{loadProfileId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});