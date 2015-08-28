Ext.define('Imt.channeldata.view.DataSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channel-data-setup',
    itemId: 'channel-data-setup',
    requires: [
        'Imt.channeldata.view.ChannelGraph'
    ],
    router: null,
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
                    type: 'fit',
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
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});