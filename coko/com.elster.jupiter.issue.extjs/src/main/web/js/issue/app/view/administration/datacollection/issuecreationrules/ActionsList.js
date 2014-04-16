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
    }
});