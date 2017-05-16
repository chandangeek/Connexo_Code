/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RequestSecurityLevels', {
    extend: 'Ext.data.Store',
    storeId: 'requestSecurityLevels',
    requires: [
        'Mdc.model.RequestSecurityLevel'
    ],
    model: 'Mdc.model.RequestSecurityLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securityproperties/reqsecuritylevels',
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