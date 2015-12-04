Ext.define('Imt.channeldata.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.channelsSetup',
    itemId: 'channelsSetup',
    mRID: null,
    router: null,
    usagepoint: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.channeldata.view.Grid',
        'Imt.channeldata.view.Preview',
//        'Imt.channeldata.view.ChannelsTopFilter'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'stepsMenu',
//                        usagepoint: me.usagepoint,
                        toggleId: 'channelsLink',
                        router: me.router,
                        mRID: me.mRID
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            layout: 'fit',
            title: Uni.I18n.translate('general.channels', 'IMT', 'Channels'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'channelsGrid',
                        mRID: me.mRID,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('channels.empty.title', 'IMT', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('channels.empty.list.item1', 'IMT', 'No channels have been defined yet.'),
                            Uni.I18n.translate('channels.empty.list.item2', 'IMT', 'No channels comply to the filter.')
                        ],
                        margins: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'channelsPreview',
                        router: me.router,
                        usagepoint: me.usagepoint
                    }
                }
            ],
//            dockedItems: [
//                {
//                    dock: 'top',
//                    xtype: 'channelsTopFilter'
//                }
//            ]
        };

        me.callParent(arguments);
    }
});