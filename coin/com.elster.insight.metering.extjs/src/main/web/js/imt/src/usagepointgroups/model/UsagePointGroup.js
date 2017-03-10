/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.model.UsagePointGroup', {
    extend: 'Uni.model.Version',
    requires: [
        'Imt.model.SearchCriteria'
    ],
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', defaultValue: true},
        {name: 'filter', type: 'auto', useNull: true, defaultValue: null},
        {name: 'usagePoints', type: 'auto', useNull: true, defaultValue: null},        
        {name: 'metrologyConfigurationIds', persist: false},
        {name: 'selectedUsagePoints', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepointgroups',
        reader: {
            type: 'json'
        }
    },
    getNumberOfSearchResults: function (callback) {
        var me = this;
        Ext.Ajax.request({
            method: 'GET',
            url: '/api/jsr/search/com.elster.jupiter.metering.UsagePoint/count',
            params: {
                filter: me.get('filter')
            },
            callback: callback
        });
    }
});