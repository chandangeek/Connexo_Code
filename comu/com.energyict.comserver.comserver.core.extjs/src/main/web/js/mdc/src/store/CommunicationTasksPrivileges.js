/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasksPrivileges', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.CommunicationTasksPrivilege',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }]
});
