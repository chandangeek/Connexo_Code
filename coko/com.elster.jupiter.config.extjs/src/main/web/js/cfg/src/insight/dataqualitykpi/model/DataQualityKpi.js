/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.model.DataQualityKpi', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'usagePointGroup', type: 'auto', defaultValue: null},
        {name: 'metrologyPurpose', type: 'auto', defaultValue: null},
        {name: 'purposes', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false},
        {name: 'previousRecurrentTasks', type: 'auto', defaultValue: null},
        {name: 'nextRecurrentTasks', type: 'auto', defaultValue: null}
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
        url: '/api/dqk/usagePointKpis'
    }
});