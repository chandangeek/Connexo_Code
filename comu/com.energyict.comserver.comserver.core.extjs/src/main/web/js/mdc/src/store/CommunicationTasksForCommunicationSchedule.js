/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasksForCommunicationSchedule',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTask'
    ],
    model: 'Mdc.model.CommunicationTask',
    storeId: 'CommunicationTasksForCommunicationSchedule',
    remoteSort: true

});