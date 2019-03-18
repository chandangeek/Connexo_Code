/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.MeterFirmwareDepsEdit', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fwc.model.FirmwareMeterDepEdit',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/field/firmwares/{deviceTypeId}/previous',
        reader: {
            type: 'json',
            root: 'firmwares'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});