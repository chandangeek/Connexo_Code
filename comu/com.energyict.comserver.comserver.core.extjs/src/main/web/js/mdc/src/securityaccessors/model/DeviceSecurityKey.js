/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.DeviceSecurityKey', {
    extend: 'Mdc.securityaccessors.model.DeviceSecurityAccessor',

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/securityaccessors/keys',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});