Ext.define('Mdc.view.setup.comservercomports.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    hideHeaders: true,
    store: 'Mdc.store.AddComPortPools',
    alias: 'widget.outboundportcomportpools',
    itemId: 'outboundportcomportpools',
    width: 538,
    columns: [
        {
            dataIndex: 'name',
            flex: 1
        },
        {
            xtype: 'actioncolumn',
            iconCls: 'icon-delete',
            align: 'right'
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
            action: 'showAddComPortPoolPage'
        }
    ]
});