Ext.define('Mdc.view.setup.devicechannels.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelData',
    itemId: 'deviceLoadProfileChannelData',
    requires: [
        'Mdc.view.setup.devicechannels.TableView',
        'Mdc.view.setup.devicechannels.GraphView',
        'Mdc.view.setup.devicechannels.SideFilter',
        'Uni.view.toolbar.PagingTop'
    ],

    router: null,
    channel: null,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                items: [
                    {
                        xtype: 'filter-top-panel',
                        itemId: 'deviceloadprofileschanneldatafilterpanel',
                        emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                    },
//                    {
//                        xtype: 'deviceLoadProfileChannelGraphView'
//                    },
                    {
                        xtype: 'deviceLoadProfileChannelTableView',
                        channel: me.channel,
                        router: me.router
                    }
                ]
            }
        ];


        me.callParent(arguments);
    }
});

