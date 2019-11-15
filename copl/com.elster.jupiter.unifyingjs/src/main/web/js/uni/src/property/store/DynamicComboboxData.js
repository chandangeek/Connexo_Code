/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.property.store.DynamicComboboxData', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.DynamicComboboxData'
    ],
    model: 'Uni.property.model.DynamicComboboxData',
    storeId: 'DynamicComboboxData',
    proxy: {
        type: 'rest',
        tempUrl: '/api/ddr/devices/{deviceId}/securityaccessors/certificates',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (url) {
            this.url = url;
        }
    }
});
