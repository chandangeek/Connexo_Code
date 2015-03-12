Ext.define('Mdc.view.setup.comservercomports.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    hideHeaders: true,
    store: 'Mdc.store.AddComPortPools',
    alias: 'widget.outboundportcomportpools',
    itemId: 'outboundportcomportpools',
    margin: '0 0 -30 0',
    width: 538,
    overflowY: 'hidden',
    autoHeight: true,
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
                    iconCls: 'uni-icon-delete',
                    handler: function (grid, rowIndex) {
                        grid.getStore().removeAt(rowIndex);
                        grid.refresh();
                    }
                }
            ]
        }
    ],
    tbar: [
        {
            xtype: 'container',
            itemId: 'comPortPoolsCount',
            html: 'No communication port pools'
        }
    ],
    rbar: [
        {
            xtype: 'container',
            items: [
                {
                    xtype: 'button',
                    itemId: 'btn-add-comport-pools',
                    text: 'Add communication port pools',
                    action: 'showAddComPortPoolPage',
                    margin: '0 0 0 10'
                }
            ]
        }
    ]
});