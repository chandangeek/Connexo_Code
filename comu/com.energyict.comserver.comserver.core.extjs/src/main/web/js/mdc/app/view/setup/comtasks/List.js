Ext.define('Mdc.view.setup.comtasks.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communication-tasks-list',
    store: 'Mdc.store.CommunicationTasks',
    enableColumnHide: false,
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
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.comtasks.ActionMenu',
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
                            store: 'Mdc.store.CommunicationTasks',
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
                    text: 'Create communication task',
                    action: 'createcommunicationtasks',
                    hrefTarget: '',
                    href: '#/administration/communicationtasks/create'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'Mdc.store.CommunicationTasks',
            dock: 'bottom'
        }
    ]
});