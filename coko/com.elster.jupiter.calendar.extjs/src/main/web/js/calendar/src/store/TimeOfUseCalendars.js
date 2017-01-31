/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cal.store.TimeOfUseCalendars', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.timeofuse.Calendar',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/cal/calendars',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'calendars'
        }
    }
});
