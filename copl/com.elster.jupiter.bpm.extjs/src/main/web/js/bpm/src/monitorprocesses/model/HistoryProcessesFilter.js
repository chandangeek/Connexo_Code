/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.HistoryProcessesFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'process', type: 'string', useNull: true},
        {name: 'startedOnFrom', type: 'number', useNull: true},
        {name: 'startedOnTo', type: 'number', useNull: true},
        {name: 'status', type: 'string', useNull: true},
        {name: 'user', type: 'string', useNull: true}
    ]
});