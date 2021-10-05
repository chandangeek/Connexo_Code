/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.Alarms', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.Alarm',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dal/alarms',
        timeout: 9999999,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
