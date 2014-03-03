Ext.define('Mtr.view.workspace.issues.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-list',
    store: 'Mtr.store.Issues',
    cls: 'content-wrapper',
    enableColumnHide: false,
    loadMask: false,
    height: 395,
    emptyText: '<h3>No issue found</h3><p>No data collection issues have been created yet.</p>',
    columns: [
        {
            header: 'Title',
            xtype: 'templatecolumn',
            tpl: '{reason}<tpl if="device"> to {device.name} {device.sNumber}</tpl>',
            flex: 2,
            sortable: false
        },
        {
            header: 'Due date',
            dataIndex: 'dueDate',
            xtype: 'datecolumn',
            format: 'M d Y',
            width: 140,
            sortable: false
        },
        {
            header: 'Status',
            dataIndex: 'status',
            width: 100,
            sortable: false
        },
        {
            header: 'Assignee',
            xtype: 'templatecolumn',
            tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.title}',
            flex: 1,
            sortable: false
        },
        {
            header: 'Actions',
            xtype: 'actioncolumn',
            iconCls: 'isu-action-icon',
            width: 70,
            align: 'left',
            sortable: false
        }
    ],
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            items: [
                {
                    xtype: 'container',
                    flex: 1,
                    items: [
                        {
                            xtype: 'pagingtoolbartop',
                            store: 'Mtr.store.Issues',
                            dock: 'top',
                            border: false
                        }
                    ]
                },
                {
                    xtype: 'button',
                    name: 'bulk-change-issues',
                    text: 'Bulk action'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'Mtr.store.Issues',
            dock: 'bottom'
        }
    ]
});