Ext.define('Isu.view.administration.communicationtasks.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communication-tasks-list',
    store: 'Isu.store.CommunicationTasks',
    enableColumnHide: false,
    emptyText: '<h3>No task found</h3><p>No communication tasks have been defined yet.</p>',
    height: 395,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header: 'Actions',
                xtype: 'actioncolumn',
                iconCls: 'x-uni-action-icon',
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
                            store: 'Isu.store.CommunicationTasks',
                            displayMsg: '{0} - {1} of {2} communication tasks',
                            displayMoreMsg: '{0} - {1} of more than {2} communication tasks',
                            emptyMsg: '0 communication tasks',
                            dock: 'top',
                            border: false
                        }
                    ]
                },
                {
                    xtype: 'button',
                    ui: 'action',
                    text: 'Create communication tasks',
                    action: 'createcommunicationtasks'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'Isu.store.CommunicationTasks',
            dock: 'bottom'
        }
    ]
});