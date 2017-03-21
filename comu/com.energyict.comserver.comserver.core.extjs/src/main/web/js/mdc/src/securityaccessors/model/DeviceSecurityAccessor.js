/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.DeviceSecurityAccessor', {
    extend: 'Uni.model.ParentVersion',

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/securityaccessors',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});