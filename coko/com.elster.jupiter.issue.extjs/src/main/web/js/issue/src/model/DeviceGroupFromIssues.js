/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.DeviceGroupFromIssues', {

    extend: 'Uni.model.Version',

    requires: [
        'Isu.model.SearchCriteria'
    ],

    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', defaultValue: false},
        {name: 'devices', type: 'auto', useNull: true, defaultValue: null}
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    },

    getNumberOfSearchResults: function (callback) {
        var me = this;

        Ext.Ajax.request({
            method: 'GET',
            url: '/api/jsr/search/com.energyict.mdc.device.data.Device/count',
            params: {
                filter: me.get('filter')
            },
            callback: callback
        });
    }

});