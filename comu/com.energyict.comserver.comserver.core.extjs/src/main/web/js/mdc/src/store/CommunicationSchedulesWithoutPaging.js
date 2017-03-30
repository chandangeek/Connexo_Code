/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationSchedulesWithoutPaging',{
    extend: 'Mdc.store.CommunicationSchedules',
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});