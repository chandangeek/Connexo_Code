/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.MeterFirmwareDeps', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fwc.model.FirmwareMeterDep',
    autoLoad: false,
    remoteFilter: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/field/devicetypes/{deviceTypeId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});