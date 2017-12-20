/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DataCollectionKpi', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
        'Mdc.model.TaskInfo'
    ],
    fields: [
        'connectionNextRecurrentTasks', 'communicationNextRecurrentTasks', 'connectionPreviousRecurrentTasks', 'communicationPreviousRecurrentTasks',
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'displayRange', type: 'auto', defaultValue: null},
        {name: 'connectionTarget', type: 'integer', useNull: true, defaultValue: null},
        {name: 'communicationTarget', type: 'integer', useNull: true, defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false},
        {name: 'communicationTaskId', type: 'integer', defaultValue: 0},
        {name: 'connectionTaskId', type: 'integer', defaultValue: 0}
    ],

    associations: [
        {
            name: 'connectionNextRecurrentTasks',
            type: 'hasMany',
            model: 'Mdc.model.TaskInfo',
            associationKey: 'connectionNextRecurrentTasks',
            foreignKey: 'connectionNextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.TaskInfo';
            }
        },
        {
            name: 'communicationNextRecurrentTasks',
            type: 'hasMany',
            model: 'Mdc.model.TaskInfo',
            associationKey: 'communicationNextRecurrentTasks',
            foreignKey: 'communicationNextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.TaskInfo';
            }
        },
        {
            name: 'connectionPreviousRecurrentTasks',
            type: 'hasMany',
            model: 'Mdc.model.TaskInfo',
            associationKey: 'connectionPreviousRecurrentTasks',
            foreignKey: 'connectionPreviousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.TaskInfo';
            }
        },
        {
            name: 'communicationPreviousRecurrentTasks',
            type: 'hasMany',
            model: 'Mdc.model.TaskInfo',
            associationKey: 'communicationPreviousRecurrentTasks',
            foreignKey: 'communicationPreviousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.TaskInfo';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/kpis'
    }
});