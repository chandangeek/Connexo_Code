Ext.define('Imt.channeldata.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channel-list-setup',
    itemId: 'channel-list-setup',
    requires: [
        'Imt.channeldata.view.ChannelList'
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
                itemId: 'channelListSetupPanel',
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
                        xtype: 'channel-list',
                        mRID: me.mRID,
                        router: me.router
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.down('#channelList').setTitle(me.mRID);
    }
});