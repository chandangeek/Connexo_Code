Ext.define('Mdc.view.setup.comportpool.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comPortPoolsGrid',
    itemId: 'comportpoolsgrid',
    requires: [
        'Mdc.store.ComPortPools',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.comportpool.ActionMenu'
    ],
    overflowY: 'auto',
    store: 'ComPortPools',
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            xtype: 'templatecolumn',
            tpl: '<a href="#/administration/comportpools/{id}/overview">{name}</a>',
            flex: 1
        },
        {
            header: Uni.I18n.translate('comPortPool.preview.direction', 'MDC', 'Direction'),
            dataIndex: 'direction'
        },
        {
            header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
            dataIndex: 'type'
        },
        {
            header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            dataIndex: 'active',
            renderer: function (value, metadata) {
                if (value === true) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        },
        {
            itemId: 'actionColumn',
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'comportpool-actionmenu',
                itemId: 'comportpoolViewMenu'
            }
        }
    ],

    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            store: 'ComPortPools',
            displayMsg: Uni.I18n.translate('comPortPool.displayMsg', 'MDC', '{0} - {1} of {2} communication port pools'),
            displayMoreMsg: Uni.I18n.translate('comPortPool.displayMoreMsg', 'MDC', '{0} - {1} of more communication port pools'),
            items: [
                {
                    xtype: 'component',
                    flex: 1
                },
                {
                    text: Uni.I18n.translate('comPortPool.addComPortPool', 'MDC', 'Add communication port pool'),
                    menu: {
                        plain: true,
                        border: false,
                        shadow: false,
                        itemId: 'addComPortPoolMenu',
                        items: [
                            {
                                text: Uni.I18n.translate('comPortPool.inbound', 'MDC', 'Inbound'),
                                action: 'addInbound'
                            },
                            {
                                text: Uni.I18n.translate('comPortPool.outbound', 'MDC', 'Outbound'),
                                action: 'addOutbound'
                            }
                        ]
                    }
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'ComPortPools',
            itemsPerPageMsg: Uni.I18n.translate('comPortPool.itemsPerPageMsg', 'MDC', 'Communication port pools per page'),
            dock: 'bottom'
        }
    ]
});