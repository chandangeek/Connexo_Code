/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.model.SecurityAccessorsOnDeviceType', {
    extend: 'Mdc.securityaccessors.model.SecurityAccessor',
    requires: [
        'Mdc.securityaccessors.model.SecurityAccessor'
    ],

    fields: [
        {name: 'wrapperAccessorId', type: 'int', useNull: true}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors',
        reader: {
            type: 'json'
        },
        setUrl: function(deviceTypeId, securityaccessorId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});