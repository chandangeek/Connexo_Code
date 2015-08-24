Ext.define('Imt.channeldata.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channel-list-setup',
    itemId: 'channel-list-setup',
    requires: [
        'Imt.channeldata.view.ChannelList'
    ],
    router: null,
    content: [
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
                    xtype: 'channel-list'
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
//        panel.title = me.router.getRoute().getTitle();
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
        this.callParent(arguments);
    }
});