Ext.define('Bpm.view.task.TasksTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'bpm-view-tasks-topfilter',
    store: 'Bpm.store.task.Tasks',
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('bpm.filter.status', 'BPM', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Bpm.store.task.TasksFilterStatuses'
            },
            {
                type: 'combobox',
                dataIndex: 'user',
                emptyText: Uni.I18n.translate('bpm.filter.assignee', 'BPM', 'Assignee'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Bpm.store.task.TasksFilterUsers'
            },
            {
                type: 'combobox',
                dataIndex: 'dueDate',
                emptyText: Uni.I18n.translate('bpm.filter.dueDate', 'BPM', 'Due date'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Bpm.store.task.TasksFilterDueDates'
            },
            {
                type: 'combobox',
                dataIndex: 'process',
                emptyText: Uni.I18n.translate('bpm.filter.process', 'BPM', 'Process'),
                multiSelect: true,
                displayField: 'id',
                valueField: 'id',
                width: 240,
                store: 'Bpm.store.task.TasksFilterProcesses'
            }
        ]
        me.callParent(arguments);
    }
    });