Ext.define('Isu.view.administration.datacollection.issuecreationrules.EditActionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issues-creation-rules-edit-actions-list',
    enableColumnHide: false,
    height: 395,
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
                dataIndex: 'when',
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
    store: Ext.create('Ext.data.Store', {
        fields: ['description', 'type', 'when'],
        data: [
            {
                "description": "Send email to Monique",
                "type": "Email",
                "when": "Issue creation"
            },
            {
                "description": "Automatically close issue when overdue",
                "type": "Close issue",
                "when": "Issue overdue"
            }
        ]
    })
});