/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.model.RegisteredDevicesKPI', {
    extend: 'Uni.model.Version',
    requires: ['Mdc.registereddevices.model.TaskInfo'],
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'target', type: 'integer', useNull: true, defaultValue: 95},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false},
        'previousRecurrentTasks', 'nextRecurrentTasks'
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
        },

    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/registereddevkpis'
    }
});
