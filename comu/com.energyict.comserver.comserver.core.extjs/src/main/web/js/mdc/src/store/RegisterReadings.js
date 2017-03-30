/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterReadings', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterReading'
    ],
    model: 'Mdc.model.RegisterReading',
    storeId: 'DeviceRegisterReadings',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 90000,
        urlTpl: '/api/ddr/devices/{0}/registers/registerreadings',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});
