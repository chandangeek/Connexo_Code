/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.Interval',
    pageSize: undefined,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/mds/loadprofiles/intervals',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }

});
