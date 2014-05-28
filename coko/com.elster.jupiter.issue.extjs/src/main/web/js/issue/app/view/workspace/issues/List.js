Ext.define('Isu.view.workspace.issues.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-list',
    store: 'Isu.store.Issues',
    emptyText: '<h3>No issue found</h3><p>No data collection issues have been created yet.</p>',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {   itemId: 'Title',
                header: 'Title',
                xtype: 'templatecolumn',
                tpl: '<a href="#/workspace/datacollection/issues/{id}">{title}</a>',
                flex: 2
            },
            {
                itemId: 'dueDate',
                header: 'Due date',
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'status',
                header: 'Status',
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'assignee',
                header: 'Assignee',
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.workspace.issues.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Isu.store.Issues',
            displayMsg: '{0} - {1} of {2} issues',
            displayMoreMsg: '{0} - {1} of more than {2} issues',
            emptyMsg: '0 issues',
            items: [
                '->',
                {
                    itemId: 'bulkAction',
                    xtype: 'button',
                    text: 'Bulk action',
                    action: 'bulkchangesissues',
                    hrefTarget: '',
                    href: '#/workspace/datacollection/bulkaction'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Isu.store.Issues',
            dock: 'bottom'
        }
    ]
});