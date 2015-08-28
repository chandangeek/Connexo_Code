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
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    }
                },
                items: [{
                    xtype: 'preview-container',    
                    grid: {
                        xtype: 'channel-list',
                        mRID: me.mRID,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-usagepoint-channel-config',
                        title: Uni.I18n.translate('usagepointchannelconfiguration.empty.title', 'IMT', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('usagepointchannelconfiguration.empty.list.item1', 'IMT', 'No device is associated with usage point.'),
                            Uni.I18n.translate('usagepointchannelconfiguration.empty.list.item2', 'IMT', 'No channels have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'container',
                        itemId: 'previewComponentContainer'
                    }
                }]
            }
        ];
        me.callParent(arguments);
        me.down('#channelList').setTitle(me.mRID);
    }
});