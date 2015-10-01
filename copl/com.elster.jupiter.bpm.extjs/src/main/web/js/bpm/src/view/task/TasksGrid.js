Ext.define('Bpm.view.task.TasksGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-tasks-grid',
    store: 'Bpm.store.task.Tasks',
    requires: [
     //   'Uni.grid.column.Action',
    //    'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'//,
    //    'Bpm.view.task.TaskActionMenu'
    ],
    
    initComponent: function () {
        var me = this;
        me.columns = [         
            {
                header: Uni.I18n.translate('bpm.task.name', 'BPM', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                xtype: 'uni-grid-column-duration',
                header: Uni.I18n.translate('bpm.task.dueDate', 'BPM', 'Due date'),
                dataIndex: 'dueDate',
                shortFormat: true,
                flex: 1
            },
            {
                xtype: 'uni-grid-column-duration',
                header: Uni.I18n.translate('bpm.task.creationDate', 'BPM', 'Creation date'),
                dataIndex: 'createdOn',
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
                dataIndex: 'priority',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.status', 'BPM', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                dataIndex: 'actualOwner',
                flex: 1
            }
            /*,
            {
                xtype: 'uni-actioncolumn',
                width: 100,
                menu: {
                    xtype: 'bpm-task-action-menu'
                }
            }*/
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.displayMsg', 'BPM', '{0} - {1} of {2} task'),
                displayMoreMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.displayMoreMsg', 'BPM', '{0} - {1} of more than {2} task'),
                emptyMsg: Uni.I18n.translate('bpm.task.pagingtoolbartop.emptyMsg', 'BPM', 'There are no task to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('bpm.task.pagingtoolbarbottom.itemsPerPage', 'BPM', 'Task per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
