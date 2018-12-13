/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.UsedCommunicationSchedulesForDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationSchedule'
    ],
    model: 'Mdc.model.CommunicationSchedule',
    storeId: 'UsedCommunicationSchedulesForDevice',
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules/used',
        reader: {
            type: 'json',
            root: 'schedules'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
