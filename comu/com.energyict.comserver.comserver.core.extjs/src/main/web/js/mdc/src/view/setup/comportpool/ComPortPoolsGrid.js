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
    store: 'Mdc.store.ComPortPools',

    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);

        me.columns= [
            {
                header: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
              //  xtype: 'templatecolumn',
              //  tpl: '<a href="#/administration/comportpools/{id}">{name}</a>',
                flex: 1,
                sortable: false,
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/comportpools/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('comPortPool.preview.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'type',
                flex: 1
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
                privileges: Mdc.privileges.Communication.admin,
                menu: {
                    xtype: 'comportpool-actionmenu',
                    itemId: 'comportpoolViewMenu'
                }
            }
        ];

            me.dockedItems= [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('comPortPool.displayMsg', 'MDC', '{0} - {1} of {2} communication port pools'),
                displayMoreMsg: Uni.I18n.translate('comPortPool.displayMoreMsg', 'MDC', '{0} - {1} of more communication port pools'),
                items: [
                    {
                        text: Uni.I18n.translate('comPortPool.addComPortPool', 'MDC', 'Add communication port pool'),
                        itemId: 'btn-add-comport-pool-menu',
                        privileges: Mdc.privileges.Communication.admin,
                        menu: {
                            plain: true,
                            border: false,
                            shadow: false,
                            itemId: 'addComPortPoolMenu',
                            items: [
                                {
                                    text: Uni.I18n.translate('comPortPool.inbound', 'MDC', 'Inbound'),
                                    action: 'addInbound',
                                    itemId: 'btn-add-inbound'
                                },
                                {
                                    text: Uni.I18n.translate('comPortPool.outbound', 'MDC', 'Outbound'),
                                    action: 'addOutbound',
                                    itemId: 'btn-add-outbound'
                                }
                            ]
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('comPortPool.itemsPerPageMsg', 'MDC', 'Communication port pools per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});
