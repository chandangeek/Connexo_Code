/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.TaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Apr.store.Tasks',
    alias: 'widget.task-management-filter',

    requires: [
        'Apr.store.QueuesByApplication',
        'Apr.store.SuspendedTask'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                dataIndex: 'application',
                value: me.applicationKey,
                hidden: true
            },
            {
                type: 'combobox',
                dataIndex: 'queue',
                emptyText: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                multiSelect: true,
                displayField: 'queue',
                valueField: 'queue',
                store: me.queuesStore
            },
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('taskManagement.startedBetween', 'APR', 'Started between')
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