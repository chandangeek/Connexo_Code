/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.RegisteredDevicesOnGateway', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.RegisteredDevicesOnGateway'
    ],
    model: 'Mdc.registereddevices.model.RegisteredDevicesOnGateway',
    storeId: 'RegisteredDevicesOnGateway',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/registereddevkpis/gateway/{gatewayId}',

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (gatewayId) {
            this.url = this.urlTpl.replace('{gatewayId}', gatewayId);
        }

    }
});

