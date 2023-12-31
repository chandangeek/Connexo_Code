/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfilesOfDevice', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.LoadProfileOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/loadprofiles',
        reader: {
            type: 'json',
            root: 'loadProfiles'
        }
    }
});