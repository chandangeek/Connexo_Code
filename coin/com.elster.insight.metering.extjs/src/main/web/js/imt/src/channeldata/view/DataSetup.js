Ext.define('Imt.channeldata.view.DataSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channel-data-setup',
    itemId: 'channel-data-setup',
    requires: [
        'Imt.channeldata.view.ChannelGraph',
        'Imt.channeldata.view.DataGrid',
        'Imt.channeldata.view.ChannelTopFilter'
    ],
    router: null,
    channel: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        mRID: me.mRID
                    }
                ]
            }
        ];
        
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'channelGraphSetupPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    }
                },
                items: [
                        {
                            xtype: 'channel-graph',
                            mRID: me.mRID,
                            router: me.router
                        },
                        {
                            xtype: 'channel-data-grid',
                            router: me.router,
                            channelRecord: me.channel,
                            height: 600
                        }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'imt-channeldata-topfilter',
                        itemId: 'channeldatafilterpanel',
                        hasDefaultFilters: true,
                        filterDefault: me.filter
                    }
                 ]
            }
        ];
        me.callParent(arguments);
    }
});