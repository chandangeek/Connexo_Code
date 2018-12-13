/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasksActions', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.CommunicationTasksAction',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }]
});
