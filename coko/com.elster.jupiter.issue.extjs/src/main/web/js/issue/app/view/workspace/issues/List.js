Ext.define('Isu.view.workspace.issues.List', {
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
    store: 'Isu.store.Issues',
    enableColumnHide: false,
    height: 395,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Title',
                xtype: 'templatecolumn',
                tpl: '<a href="#/workspace/datacollection/issues/{id}">{reason.name}<tpl if="device"> to {device.name} {device.serialNumber}</tpl></a>',
                flex: 2
            },
            {
                header: 'Due date',
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                header: 'Status',
                xtype: 'templatecolumn',
                tpl: '<tpl if="status">{status.name}</tpl>',
                width: 100
            },
            {
                header: 'Assignee',
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                flex: 1
            },
            {
                header: 'Actions',
                xtype: 'actioncolumn',
                iconCls: 'isu-action-icon',
                width: 70,
                align: 'left'
            }
        ]
    },
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
                            store: 'Isu.store.Issues',
                            displayMsg: '{0} - {1} of {2} issues',
                            displayMoreMsg: '{0} - {1} of more than {2} issues',
                            emptyMsg: '0 issues',
                            dock: 'top',
                            border: false
                        }
                    ]
                },
                {
                    xtype: 'button',
                    text: 'Bulk action',
                    action: 'bulkchangesissues',
                    hrefTarget: '',
                    href: '#/workspace/datacollection/bulkaction'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'Isu.store.Issues',
            dock: 'bottom'
        }
    ]
});