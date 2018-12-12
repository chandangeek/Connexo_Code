/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.SupportedFirmwareTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.model.FirmwareType'
    ],
    model: 'Fwc.model.FirmwareType',
    storeId: 'SupportedFirmwareTypes',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/supportedfirmwaretypes',
        reader: {
            type: 'json',
            root: 'firmwareTypes'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }

    }
});