/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.store.HsmCapabilities', {
    extend: 'Ext.data.Store',
    storeId: 'hsmCapabilitiesStore',
    autoLoad: false,
    fields: [{
        name: 'name',
        mapping: function (data) {
            return data;
        }
    }],
    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors/hsm/capabilities',
        reader: {
            type: 'array'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});