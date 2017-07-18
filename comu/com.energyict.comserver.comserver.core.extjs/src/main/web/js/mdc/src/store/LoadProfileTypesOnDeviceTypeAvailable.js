/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfileTypesOnDeviceTypeAvailable', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileTypeOnDeviceType'
    ],
    model: 'Mdc.model.LoadProfileTypeOnDeviceType',
    storeId: 'LoadProfileTypesOnDeviceTypeAvailable',

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/loadprofiletypes',
        reader: {
            type: 'json',
            root: 'data'
        },
        extraParams: {
            available: true
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});