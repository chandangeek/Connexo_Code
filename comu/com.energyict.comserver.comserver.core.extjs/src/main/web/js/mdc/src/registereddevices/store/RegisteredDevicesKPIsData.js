/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.RegisteredDevicesKPIsData', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.RegisteredDevicesKPIsData'
    ],
    model: 'Mdc.registereddevices.model.RegisteredDevicesKPIsData',
    storeId: 'RegisteredDevicesKPIsData',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/registereddevkpis/{kpiId}/data',

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (kpiId) {
            this.url = this.urlTpl.replace('{kpiId}', kpiId);
        }
    }
});

