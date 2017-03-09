/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.DestinationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dxp-tasks-destinations-grid',
    router: null,
    store: Ext.create('Ext.data.Store', {
        model: 'Dxp.model.Destination'
    }),
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dxp.view.tasks.DestinationActionMenu',
        'Dxp.model.Destination'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.method', 'DES', 'Method'),
                dataIndex: 'method',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.destination', 'DES', 'Destination'),
                renderer : function(val, meta, record) {
                    meta.tdAttr = 'data-qtip="' + record.data.tooltiptext + '"';
                    return Ext.htmlEncode(val);
                },
                dataIndex: 'destination',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'dxp-tasks-destination-action-menu',
                    itemId: 'dxp-tasks-destination-action-menu'
                }
            }
        ]

        me.callParent(arguments);
    }
});

