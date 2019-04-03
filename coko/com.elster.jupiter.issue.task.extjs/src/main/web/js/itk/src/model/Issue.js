/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        {name: 'task_data', persist: false, mapping: 'id'},
        {
            name: 'failedTaskData',
            persist: false,
            mapping: function (data) {

            }

        }
        ],

    proxy: {
        type: 'rest',
        url: '/api/itk/issues',
        reader: {
            type: 'json'
        }
    }
});