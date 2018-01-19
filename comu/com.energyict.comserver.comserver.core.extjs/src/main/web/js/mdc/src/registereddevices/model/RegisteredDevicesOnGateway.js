/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.model.RegisteredDevicesOnGateway', {
    extend: 'Uni.model.Version',
    fields: [
        {
            name: 'timestamp',
            type: 'integer',
            useNull: true
        },
        {
            name: 'registered',
            type: 'integer',
            useNull: true,
            convert: function (value, rec) {
                return rec.get('timestamp') < new Date().getTime() ? value : 0;
            }
        }
    ],
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
