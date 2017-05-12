/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ConfigurationSecurityProperties', {
    extend: 'Ext.data.Store',
    storeId: 'configurationSecurityProperties',
    requires: [
        'Uni.property.model.Property'
    ],
    model: 'Uni.property.model.Property',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securityproperties/confsecurityproperties',
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