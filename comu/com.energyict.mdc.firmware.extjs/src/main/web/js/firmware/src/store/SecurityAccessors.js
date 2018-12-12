/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.SecurityAccessors', {
    extend: 'Ext.data.Store',
    // requires: [
    //     'Fwc.model.SecurityAccessor'
    // ],
    fields: ['id', 'name'],
    storeId: 'SecurityAccessor',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/securityaccessors/availablesecurityaccessors',
        reader: {
            type: 'json',
            root: 'securityaccessors'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});
