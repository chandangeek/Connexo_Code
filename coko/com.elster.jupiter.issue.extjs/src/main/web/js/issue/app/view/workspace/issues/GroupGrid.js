Ext.define('Isu.view.workspace.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.store.IssuesGroups'
    ],
    alias: 'widget.issue-group-grid',
    store: 'Isu.store.IssuesGroups',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'reason',
                text: 'Reason',
                dataIndex: 'reason',
                flex: 5
            },
            {
                itemId: 'issues_num',
                text: 'Issues',
                dataIndex: 'number',
                flex: 1
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Isu.store.IssuesGroups',
            displayMsg: '{0} - {1} of {2} reasons',
            displayMoreMsg: '{0} - {1} of more than {2} reasons',
            emptyMsg: '0 reasons'
        },
        {
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            store: 'Isu.store.IssuesGroups',
            itemsPerPageMsg: 'Items per page'
        }
    ]
});