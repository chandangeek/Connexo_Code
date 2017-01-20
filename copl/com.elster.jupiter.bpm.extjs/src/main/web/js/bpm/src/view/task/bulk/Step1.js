Ext.define('Bpm.view.task.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.view.grid.BulkSelection'
    ],
    alias: 'widget.tasks-bulk-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'step1-error-message',
                width: 400,
                hidden: true
            },
            {
                xtype: 'bulk-selection-grid',
                itemId: 'tasks-bulk-selection-grid',
                store: 'Bpm.store.task.TasksBuffered',

                counterTextFn: function (count) {
                    return Uni.I18n.translatePlural('general.nrOfTasks.selected', count, 'BPM',
                        'No tasks selected', '{0} task selected', '{0} tasks selected'
                    );
                },

                allLabel: Uni.I18n.translate('bpm.task.bulk.allLabel', 'BPM', 'All tasks'),
                allDescription: Uni.I18n.translate('bpm.task.bulk.allDescription', 'BPM', 'Select all tasks (related to filters and grouping on the tasks  screen)'),

                selectedLabel: Uni.I18n.translate('bpm.task.bulk.selectedLabel', 'BPM', 'Selected tasks'),
                selectedDescription: Uni.I18n.translate('bpm.task.bulk.selectedDescription', 'BPM', 'Select tasks in table'),

                bottomToolbarHidden: true,

                radioGroupName: 'selected-tasks',

                columns: [
                    {
                        itemId: 'name',
                        text: Uni.I18n.translate('bpm.task.name', 'BPM', 'Task'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        itemId: 'dueDate',
                        text: Uni.I18n.translate('bpm.task.dueDate', 'BPM', 'Due date'),
                        dataIndex: 'dueDateDisplay',
                        flex: 1,
                        shortFormat: true
                    },
                    {
                        header: Uni.I18n.translate('bpm.task.processId', 'BPM', 'Process ID'),
                        dataIndex: 'processInstancesId',
                        flex: 1
                    },
                    {
                        itemId: 'processName',
                        text: Uni.I18n.translate('bpm.task.process', 'BPM', 'Process'),
                        dataIndex: 'processName',
                        flex: 1

                    },
                    {
                        itemId: 'priority',
                        text: Uni.I18n.translate('bpm.task.priority', 'BPM', 'Priority'),
                        dataIndex: 'priorityDisplay',
                        flex: 1
                    },
                    {
                        itemId: 'status',
                        text: Uni.I18n.translate('bpm.task.status', 'BPM', 'Status'),
                        dataIndex: 'statusDisplay',
                        flex: 1
                    },
                    {
                        itemId: 'workgroup',
                        text: Uni.I18n.translate('general.workgroup', 'BPM', 'Workgroup'),
                        dataIndex: 'workgroup',
                        flex: 1,
                        renderer: function (value, metaData, record, rowIndex, colIndex) {
                            var result = '';
                            if (!Ext.isEmpty(value)) {
                                result += Ext.String.htmlEncode(value);
                            } else {
                                result = '-'
                            }
                            return result;
                        }
                    },
                    {
                        itemId: 'assignee',
                        text: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'User'),
                        dataIndex: 'actualOwner',
                        flex: 1,
                        renderer: function (value, metaData, record, rowIndex, colIndex) {
                            var result = '';
                            if (!Ext.isEmpty(value)) {
                                result += Ext.String.htmlEncode(value);
                            } else {
                                result = '-'
                            }
                            return result;
                        }
                    }
                ]
            },
            {
                xtype: 'component',
                itemId: 'selection-grid-error',
                cls: 'x-form-invalid-under',
                margin: '-30 0 0 0',
                html: Uni.I18n.translate('bpm.task.bulk.selectionGridError', 'BPM', 'Select at least one task'),
                hidden: true
            }
        ];

        me.callParent(arguments);
    }
});