/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.Triggers', {
    extend: 'Ext.data.Model',
    requires: [
        'Apr.model.TaskInfo',
        'Apr.model.Task'
    ],
    fields: [
        'id', 'application', 'name', 'type', 'previousRecurrentTasks', 'nextRecurrentTasks', 'recurrentTask'
    ],
    associations: [
        {
            name: 'previousRecurrentTasks',
            type: 'hasMany',
            model: 'Apr.model.TaskInfo',
            associationKey: 'previousRecurrentTasks',
            foreignKey: 'previousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskInfo';
            }
        },
        {
            name: 'nextRecurrentTasks',
            type: 'hasMany',
            model: 'Apr.model.TaskInfo',
            associationKey: 'nextRecurrentTasks',
            foreignKey: 'nextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskInfo';
            }
        },
        {
            name: 'recurrentTask',
            type: 'hasOne',
            model: 'Apr.model.Task',
            associationKey: 'recurrentTask',
            foreignKey: 'recurrentTask',
            getterName: 'getRecurrentTask',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.Task';
            }
        }



    ],
    proxy: {
        type: 'rest',
        url: '/api/tsk/task/triggers',
        reader: {
            type: 'json'
        }
    }
});
