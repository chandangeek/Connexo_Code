/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ResponseSecurityLevels', {
    extend: 'Ext.data.Store',
    storeId: 'responseSecurityLevels',
    requires: [
        'Mdc.model.ResponseSecurityLevel'
    ],
    model: 'Mdc.model.ResponseSecurityLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securityproperties/respsecuritylevels',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(deviceTypeId, deviceConfigId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId).replace('{deviceConfigId}', deviceConfigId);
        }
    }
});