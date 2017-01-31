/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.OverviewFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'id', persist: false},
        { name: 'deviceGroup', type: 'auto' }
    ]
});