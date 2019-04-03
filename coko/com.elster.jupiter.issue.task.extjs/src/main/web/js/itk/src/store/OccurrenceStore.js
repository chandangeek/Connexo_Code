/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ikt.store.OccurrenceStore', {
    extend: 'Ext.data.Store',

    fields: [
        {name: 'id', type: 'auto'},
        //{name: 'recurrentTask', type: 'auto'},
        {name: 'triggerTime', type: 'auto'},
        {name: 'startDate', type: 'auto'},
        {name: 'enddate', type: 'auto'},
        {name: 'status', type: 'auto'},
        {name: 'errorMessage', type: 'auto'},
        {name: 'failureTime', type: 'auto'}
    ]
});