/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.HistoryFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'startedOnFrom', type: 'number', useNull: true },
        { name: 'startedOnTo', type: 'number', useNull: true },
        { name: 'finishedOnFrom', type: 'number', useNull: true },
        { name: 'finishedOnTo', type: 'number', useNull: true }
    ]
});