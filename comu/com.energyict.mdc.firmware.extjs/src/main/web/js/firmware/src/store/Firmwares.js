/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.Firmwares', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Fwc.model.Firmware'
    ],
    model: 'Fwc.model.Firmware',
    storeId: 'Firmwares',
    autoLoad: false,
    remoteSort: true,
    hydrator: 'Uni.util.IdHydrator',
    //sorters: [{
    //    property: 'firmwareVersion',
    //    direction: 'DESC'
    //}],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares',
            totalProperty: 'total'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});
