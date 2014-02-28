Ext.define('Mtr.view.party.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.partyList',
    itemId: 'partyList',
    title: 'All parties',
    store: 'Parties',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Id', dataIndex: 'id' },
            { header: 'MRID', dataIndex: 'mRID' },
            { header: 'Name', dataIndex: 'name' },
            { header: 'Alias name', dataIndex: 'aliasName' }
        ]
    },
    initComponent: function () {
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top'
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom'
            }
        ];
        this.callParent(arguments);
    }
});