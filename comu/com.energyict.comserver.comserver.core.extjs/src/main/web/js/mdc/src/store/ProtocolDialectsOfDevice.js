/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ProtocolDialectsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceProtocolDialect'
    ],
    model: 'Mdc.model.DeviceProtocolDialect',
    storeId: 'ProtocolDialectsOfDevice',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/protocoldialects',
        reader: {
            type: 'json',
            root: 'protocolDialects'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});