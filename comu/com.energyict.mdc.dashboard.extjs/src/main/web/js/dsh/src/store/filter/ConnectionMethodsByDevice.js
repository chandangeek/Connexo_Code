/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.filter.ConnectionMethodsByDevice', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dsr/field/device/{deviceName}/connectionmethods',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'connectionmethods'
        },
        setUrl: function (deviceName) {
            this.url = this.urlTpl.replace('{deviceName}', deviceName);
        }
    }
});
