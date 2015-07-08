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
                dataIndex: 'destination',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'dxp-tasks-destination-action-menu',
                    itemId: 'dxp-tasks-destination-action-menu'
                }
            }
        ]

        me.callParent(arguments);
    }
});

