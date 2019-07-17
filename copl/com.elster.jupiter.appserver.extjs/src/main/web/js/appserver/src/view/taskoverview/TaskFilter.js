/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.TaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Apr.store.Tasks',
    xtype: 'taskFilter',

    requires:[
        'Apr.store.Applications',
        'Apr.store.Queues',
        'Apr.store.SuspendedTask',
        'Apr.store.TasksQueueTypes'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'application',
                emptyText: Uni.I18n.translate('general.application', 'APR', 'Application'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Apr.store.Applications'
            },
            {
                type: 'combobox',
                dataIndex: 'queueType',
                emptyText: Uni.I18n.translate('general.queueType', 'APR', 'Queue type'),
                multiSelect: true,
                displayField: 'queueType',
                valueField: 'queueType',
                store: 'Apr.store.TasksQueueTypes',
                matchFieldWidth: false,
                itemid: 'task-queue-type'
            },
            {
                type: 'combobox',
                dataIndex: 'queue',
                emptyText: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                multiSelect: true,
                displayField: 'queue',
                valueField: 'queue',
                store: 'Apr.store.Queues'
            },
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('validationtask.historyFilter.startedBetween', 'APR', 'Started between')
            },
            {
                type: 'combobox',
                store: 'Apr.store.SuspendedTask',
                dataIndex: 'suspended',
                emptyText: Uni.I18n.translate('general.suspended', 'APR', 'Suspended'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'value',
                queryMode: 'local',
                editable:false,
                listeners:{
                    beforeselect : function(combo, record, index){
                        combo.suspendEvents();
                        combo.clearValue();
                        combo.select(record);
                        combo.resumeEvents();
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});