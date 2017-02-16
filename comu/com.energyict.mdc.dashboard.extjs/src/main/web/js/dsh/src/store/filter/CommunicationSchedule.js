/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.filter.CommunicationSchedule', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comschedules',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'comSchedules'
        }
    }
});

