Ext.define('Isu.view.workspace.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.store.IssuesGroups'
    ],
    alias: 'widget.issue-group-grid',
    store: 'Isu.store.IssuesGroups',
    itemId: 'GroupGrid',
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
    tbar: {
        itemId: '#pagingtoolbar',
        xtype: 'pagingtoolbartop',
        store: 'Isu.store.IssuesGroups',
        displayMsg: '{0} - {1} of {2} reasons',
        displayMoreMsg: '{0} - {1} of more than {2} reasons',
        emptyMsg: '0 reasons'
    },
    bbar: {
        itemId: '#pagingtoolbar',
        xtype: 'pagingtoolbarbottom',
        store: 'Isu.store.IssuesGroups'
    }
});