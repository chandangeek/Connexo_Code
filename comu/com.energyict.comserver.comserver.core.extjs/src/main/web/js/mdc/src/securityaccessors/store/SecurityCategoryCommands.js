/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.SecurityCategoryCommands', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.SecurityCategoryCommand'
    ],
    autoLoad: false,
    model: 'Mdc.securityaccessors.model.SecurityCategoryCommand',

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors/securitycategorycommands',
        reader: {
            type: 'json'
        },
        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});