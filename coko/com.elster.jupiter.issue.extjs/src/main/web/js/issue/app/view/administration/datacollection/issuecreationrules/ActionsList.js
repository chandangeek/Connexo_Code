Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issues-creation-rules-actions-list',
    store: Ext.create('Ext.data.Store', {
        fields: [
            {name: 'description', type: 'string'},
            {name: 'type', type: 'string'},
            {name: 'whenToPerform', type: 'string'}
        ],
        data: []
    }),
    height: 165,
    enableColumnHide: false,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: 'Description',
                dataIndex: 'description',
                flex: 2
            },
            {
                header: 'Type',
                dataIndex: 'type',
                flex: 1
            },
            {
                header: 'When to perform',
                dataIndex: 'whenToPerform',
                flex: 1
            },
            {
                header: 'Action',
                xtype: 'actioncolumn',
                iconCls: 'isu-action-icon',
                align: 'left',
                width: 70
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            layout: 'hbox',
            items: [
                {
                    xtype: 'component',
                    html: '0 actions',
                    flex: 1
                },
                {
                    xtype: 'button',
                    text: 'Add action',
                    disabled: true
                }
            ]
        }
    ]
});