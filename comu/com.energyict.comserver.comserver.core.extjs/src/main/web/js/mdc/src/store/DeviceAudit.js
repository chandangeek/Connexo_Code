/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceAudit', {
    extend: 'Ext.data.Store',
    require: [
        'Cfg.audit.model.Audit'
    ],
    model: 'Cfg.audit.model.Audit',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/history/audit',
        reader: {
            type: 'json',
            root: 'audit'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});
