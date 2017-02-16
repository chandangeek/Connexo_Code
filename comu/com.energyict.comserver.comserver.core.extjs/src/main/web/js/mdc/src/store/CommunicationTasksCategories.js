/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasksCategories', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.CommunicationTasksCategory',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }]
});
