/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.filter.RegisterGroups', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{0}/registers/registergroups',

        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'registerGroups'
        },

        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});
