Ext.define('ViewDataCollectionIssues.view.ViewDataCollectionIssues', {
    extend: 'Ext.grid.Panel',
    requires:[
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'ViewDataCollectionIssues.utility.Paging',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action'
    ],
    xtype: 'view-data-collection-issues',
    store: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',
    border: 1,
    columns: [{
        header: 'Title',
        xtype: 'templatecolumn',
        tpl: '{reason}<tpl if="device"> to {device.sNumber}</tpl>',
        flex: 1
    },{
        header:    'Due Date',
        dataIndex: 'dueDate',
        xtype: 'datecolumn',
        format: 'M d Y h:m',
        width: 140
    },{
        header:    'Status',
        dataIndex: 'status',
        flex: 1
    },{
        header:    'Assignee',
        xtype: 'templatecolumn',
        tpl: '<tpl if="assignee.type"><img src="resources/icons/{assignee.type}.png"></tpl> {assignee.title}',
        flex: 1
    },{
        header: 'Actions',
        xtype:'actioncolumn',
        items: [{
            icon: 'resources/icons/actions.png'  
        }],
        width: 80,
        align: 'center'
    }],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        ui: 'footer',
        border: false,
        items: [{
            xtype: 'container',
            flex: 1
        },{
            xtype: 'button',
            text: 'Bulk action',
            disabled: true
        }]
    },{
        xtype: 'pagingpanel',
        dock: 'bottom',
        store: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',
        border: false,
        flex: 1,
        ui: 'footer',
        perPageText: 'Issues per page'
    }]
});