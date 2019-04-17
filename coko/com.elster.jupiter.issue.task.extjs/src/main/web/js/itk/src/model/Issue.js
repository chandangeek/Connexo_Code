/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        {name: 'task_data', persist: false, mapping: 'id'},
        {name: 'recurrentTask', type: 'auto'},
        {name: 'taskOccurrences', type: 'auto'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/itk/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});