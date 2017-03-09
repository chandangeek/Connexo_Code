/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterHistoryData', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterHistoryData',
        //  'Mdc.store.RegisterDataDurations'
    ],

    model: 'Mdc.model.RegisterHistoryData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 60000,
        urlTpl: '/api/ddr/devices/{deviceId}/registers/{registerId}/historydata',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (deviceId, registerId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId).replace('{registerId}', registerId);
        }
    }
});