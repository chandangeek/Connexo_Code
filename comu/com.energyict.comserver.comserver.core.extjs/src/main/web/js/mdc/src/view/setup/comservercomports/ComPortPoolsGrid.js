Ext.define('Mdc.view.setup.comservercomports.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    hideHeaders: true,
    store: 'Mdc.store.AddComPortPools',
    alias: 'widget.outboundportcomportpools',
    itemId: 'outboundportcomportpools',
    margin: '0 0 -30 0',
    width: 538,
    columns: [
        {
            dataIndex: 'name',
            flex: 1
        },
        {
            xtype: 'actioncolumn',
            align: 'right',
            items: [
                {
                    iconCls: 'icon-delete',
                    handler: function (grid, rowIndex) {
                        grid.getStore().removeAt(rowIndex);
                        grid.refresh();
                    }
                }
            ]
        }
    ],
    height: 220,
    tbar: [
        {
            xtype: 'container',
            itemId: 'comPortPoolsCount',
            html: 'No communication port pools'
        }
    ],
    rbar: [
        {
            xtype: 'button',
            text: 'Add communication port pools',
            action: 'showAddComPortPoolPage',
            margin: '0 0 0 10'
        }
    ]
});