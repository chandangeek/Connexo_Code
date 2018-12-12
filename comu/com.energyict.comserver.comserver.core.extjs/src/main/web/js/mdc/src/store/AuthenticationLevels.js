/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AuthenticationLevels', {
    extend: 'Ext.data.Store',
    storeId: 'AuthenticationLevels',
    requires: [
        'Mdc.model.AuthenticationLevel'
    ],
    model: 'Mdc.model.AuthenticationLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securityproperties/authlevels',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (deviceTypeId, deviceConfigId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId).replace('{deviceConfigId}', deviceConfigId);
        }
    }
});