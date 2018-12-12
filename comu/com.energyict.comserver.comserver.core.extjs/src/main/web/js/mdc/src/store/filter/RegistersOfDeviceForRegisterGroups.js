/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.filter.RegistersOfDeviceForRegisterGroups', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'isBilling', type: 'boolean'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{0}/registers/registersforgroups',

        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'registers'
        },

        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});
