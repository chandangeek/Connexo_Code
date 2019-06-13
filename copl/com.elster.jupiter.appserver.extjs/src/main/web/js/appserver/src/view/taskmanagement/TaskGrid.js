/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.TaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.task-management-grid',
    store: 'Apr.store.Tasks',
    router: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Apr.store.Tasks',
        'Apr.view.taskmanagement.ActionMenu'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.task', 'APR', 'Task'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/taskmanagement/viewTaskManagement').buildUrl({
                        type: record.get('queue'),
                        taskManagementId: record.get('id')
                    });
                    return '<a href="' + url + '">' + value + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                dataIndex: 'queue',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
                dataIndex: 'queueStatusString',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.suspendedTask', 'APR', 'Suspended_grid_multisense11'),  // Lau
                dataIndex: 'suspendUntil123',
                flex: 1,
                renderer: function(value){ //Lau
                    return value  ? Uni.I18n.translate('general.suspended.yes','APR','Yes') : Uni.I18n.translate('general.suspended.no','APR','No')
                    //return value!='' ? Mdc.util.ScheduleToStringConverter.convert(value) || Uni.I18n.translate('general.undefined', 'MDC', 'Undefined') : '-';
                    // return "Yes - has been suspended until next run";
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                //privileges: ,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    var taskType = record.get('queue'),
                        taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

                    return taskManagement == undefined || !taskManagement.controller.canAdministrate();
                },
                menu: {
                    xtype: 'task-management-action-menu',
                    itemId: 'task-management-action-menu'
                }
            }

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('taskManagement.pagingtoolbartop.displayMsg', 'APR', '{0} - {1} of {2} tasks'),
                displayMoreMsg: Uni.I18n.translate('taskManagement.pagingtoolbartop.displayMoreMsg', 'APR', '{0} - {1} of more than {2} tasks'),
                emptyMsg: Uni.I18n.translate('taskManagement.pagingtoolbartop.emptyMsg', 'APR', 'There are no tasks to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btnAddTask',
                        text: Uni.I18n.translate('taskManagement.general.addTask', 'APR', 'Add task'),
                        privileges: function () {
                            return Apr.TaskManagementApp.canAdministrate();
                        },
                        href: me.addTaskRoute
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('taskManagement.pagingtoolbarbottom.itemsPerPage', 'APR', 'Tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

