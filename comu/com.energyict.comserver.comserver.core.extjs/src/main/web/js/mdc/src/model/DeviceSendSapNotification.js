/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceSendSapNotification', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'auto', useNull: false}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/sap/devices/{deviceId}/sendregisterednotification',
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        },
        actionMethods: {
            create: 'POST',
            read: 'GET',
            update: 'POST',
            destroy: 'POST'
        }
    }
});