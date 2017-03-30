/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    store: 'Mdc.store.AddComPortPools',
    alias: 'widget.outboundportcomportpools',
    itemId: 'outboundportcomportpools',
    margin: '0 0 -30 0',
    hideHeaders: true,
    autoHeight: true,
    overflowY: 'hidden',
    width: 538,
    requires: [
        'Uni.grid.column.RemoveAction'
    ],

    listeners: {
        afterrender: function (grid) {
            grid.view.on('refresh', function () {
                if (grid.store.getCount() < 1) {
                    grid.setHeight(200);
                }
            })
        }
    },
    columns: [
        {
            dataIndex: 'name',
            flex: 1
        },
        {
            xtype: 'uni-actioncolumn-remove',
            align: 'right',
            handler: function (grid, rowIndex) {
                grid.getStore().removeAt(rowIndex);
                grid.refresh();
            }
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
                    text: Uni.I18n.translate('comPortPools.addComPortPools','MDC','Add communication port pools'),
                    action: 'showAddComPortPoolPage',
                    margin: '0 0 0 10'
                }
            ]
        }
    ]
});