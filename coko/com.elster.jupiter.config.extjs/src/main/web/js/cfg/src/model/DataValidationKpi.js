/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.DataValidationKpi', {
    extend: 'Ext.data.Model',
    requires: ['Cfg.model.TaskInfo'],
    fields: ['previousRecurrentTasks', 'nextRecurrentTasks',
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'version', type: 'auto', defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    associations: [
        {
            name: 'previousRecurrentTasks',
            type: 'hasMany',
            model: 'Cfg.model.TaskInfo',
            associationKey: 'previousRecurrentTasks',
            foreignKey: 'previousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Cfg.model.TaskInfo';
            }
        },
        {
            name: 'nextRecurrentTasks',
            type: 'hasMany',
            model: 'Cfg.model.TaskInfo',
            associationKey: 'nextRecurrentTasks',
            foreignKey: 'nextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Cfg.model.TaskInfo';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dqk/deviceKpis'
    }
});