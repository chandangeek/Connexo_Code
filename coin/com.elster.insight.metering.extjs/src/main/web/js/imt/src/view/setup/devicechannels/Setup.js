Ext.define('Imt.view.setup.devicechannels.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsSetup',
    itemId: 'deviceLoadProfileChannelsSetup',
    mRID: null,
    router: null,
    device: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.view.setup.devicechannels.Grid',
        'Imt.view.setup.devicechannels.Preview',
//        'Imt.view.setup.devicechannels.ChannelsTopFilter'
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
//                        device: me.device,
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
                        xtype: 'deviceLoadProfileChannelsGrid',
                        mRID: me.mRID,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('devicechannels.empty.title', 'IMT', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('devicechannels.empty.list.item1', 'IMT', 'No channels have been defined yet.'),
                            Uni.I18n.translate('devicechannels.empty.list.item2', 'IMT', 'No channels comply to the filter.')
                        ],
                        margins: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileChannelsPreview',
                        router: me.router,
                        device: me.device
                    }
                }
            ],
//            dockedItems: [
//                {
//                    dock: 'top',
//                    xtype: 'mdc-view-setup-devicechannels-channelstopfilter'
//                }
//            ]
        };

        me.callParent(arguments);
    }
});