/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.TaskGroup', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.model.task.TaskForm'
    ],

    fields: [
        'id',
        'name',
        'processName',
        'version',
        'count',
        'hasMandatory',
        'taskIds',
        {name: 'tasksForm', type: 'auto', defaultValue: null}
    ],
    associations: [
        {
            instanceName: 'tasksForm',
            name: 'tasksForm',
            type: 'hasOne',
            model: 'Bpm.model.task.TaskForm',
            associationKey: 'tasksForm',
            foreignKey: 'tasksForm',
            getTypeDiscriminator: function (node) {
                return 'Bpm.model.task.TaskForm';
            }
        }
    ]
});