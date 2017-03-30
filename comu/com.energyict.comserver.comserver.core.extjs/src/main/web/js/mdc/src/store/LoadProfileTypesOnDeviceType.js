/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileTypeOnDeviceType'
    ],
    model: 'Mdc.model.LoadProfileTypeOnDeviceType',
    storeId: 'LoadProfileTypesOnDeviceType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/loadprofiletypes',
        reader: {
            type: 'json',
            root: 'data'
        },
        extraParams: {
            available: false
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});