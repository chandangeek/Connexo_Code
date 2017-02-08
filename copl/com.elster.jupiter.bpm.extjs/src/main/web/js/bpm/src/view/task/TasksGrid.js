/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TasksGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-tasks-grid',
    router: null,
    store: 'Bpm.store.task.Tasks',
    requires: [
        //   'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'//,
        //    'Bpm.view.task.TaskActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('bpm.task.name', 'BPM', 'Task'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('workspace/tasks/task').buildUrl({taskId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
            },
            {
                xtype: 'uni-grid-column-duration',
                header: Uni.I18n.translate('bpm.task.dueDate', 'BPM', 'Due date'),
                dataIndex: 'dueDateDisplay',
                shortFormat: true,
                flex: 1
            },
            {
                xtype: 'uni-grid-column-duration',
                header: Uni.I18n.translate('bpm.task.creationDate', 'BPM', 'Creation date'),
                dataIndex: 'createdOnDisplay',
                shortFormat: true,
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.processId', 'BPM', 'Process ID'),
                dataIndex: 'processInstancesId',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.process', 'BPM', 'Process'),
                dataIndex: 'processName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.priority', 'BPM', 'Priority'),
                dataIndex: 'priorityDisplay',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.status', 'BPM', 'Status'),
                dataIndex: 'statusDisplay',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.workgroupAssignee', 'BPM', 'Workgroup'),
                dataIndex: 'workgroup',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                }
            },
            {
                header: Uni.I18n.translate('bpm.task.userAssignee', 'BPM', 'User'),
                dataIndex: 'actualOwner',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Bpm.privileges.BpmManagement.assignOrExecute,
                width: 100,
                menu: {
                    xtype: 'bpm-task-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.displayMsg', 'BPM', '{0} - {1} of {2} tasks'),
                displayMoreMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.displayMoreMsg', 'BPM', '{0} - {1} of more than {2} tasks'),
                emptyMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.emptyMsg', 'BPM', 'There are no task to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-tasks-bulk-action',
                        text: Uni.I18n.translate('bpm.task.bulkActions', 'BPM', 'Bulk action'),
                        privileges: Bpm.privileges.BpmManagement.assignOrExecute
                    }
                ]

            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('bpm.task.pagingtoolbarbottom.itemsPerPage', 'BPM', 'Tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
