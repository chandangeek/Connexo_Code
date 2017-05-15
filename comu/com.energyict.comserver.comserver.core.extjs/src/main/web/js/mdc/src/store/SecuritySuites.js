/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.SecuritySuites', {
    extend: 'Ext.data.Store',
    storeId: 'securitySuites',
    requires: [
        'Mdc.model.SecuritySuite'
    ],
    model: 'Mdc.model.SecuritySuite',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securityproperties/securitysuites',
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