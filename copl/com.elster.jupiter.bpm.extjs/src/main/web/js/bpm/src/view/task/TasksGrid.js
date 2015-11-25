Ext.define('Bpm.view.task.TasksGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-tasks-grid',
    store: 'Bpm.store.task.Tasks',
    requires: [
        //   'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Bpm.privileges.BpmManagement'
        //    'Bpm.view.task.TaskActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('bpm.task.name', 'BPM', 'Task'),
                dataIndex: 'name',
                flex: 2
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
                header: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                dataIndex: 'actualOwner',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    var result = '';
                    if (!Ext.isEmpty(value)) {
                        result = '<span class="isu-icon-USER isu-assignee-type-icon" data-qtip="' + Uni.I18n.translate('bpm.view.assignee.tooltip.user', 'BPM', 'User') + '"></span> ';
                        result += Ext.String.htmlEncode(value);
                    }
                    return result;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                //privileges: Bpm.privileges.BpmManagement.assignAndExecute,
                //privileges: Bpm.privileges.BpmManagement.assignOrExecute,
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
                        privileges: Bpm.privileges.BpmManagement.assignAndExecute
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
