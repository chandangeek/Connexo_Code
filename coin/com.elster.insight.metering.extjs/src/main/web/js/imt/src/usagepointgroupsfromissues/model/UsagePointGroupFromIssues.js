/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.model.UsagePointGroupFromIssues', {

    extend: 'Uni.model.Version',

    requires: [
        'Imt.model.SearchCriteria'
    ],

    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', defaultValue: false},
        {name: 'usagePoints', type: 'auto', useNull: true, defaultValue: null},
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepointgroups',
        reader: {
            type: 'json'
        }
    },

    getNumberOfSearchResults: function (callback) {
        Ext.Ajax.request({
            method: 'GET',
            url: '/api/jsr/search/com.elster.jupiter.metering.UsagePoint/count',
            params: {
                filter: this.get('filter')
            },
            callback: callback
        });
    }
});