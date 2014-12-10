Ext.define('Mdc.view.setup.deviceloadprofilechannels.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsSetup',
    itemId: 'deviceLoadProfileChannelsSetup',
    mRID: null,
    router: null,
    device: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofilechannels.Grid',
        'Mdc.view.setup.deviceloadprofilechannels.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'channelsLink',
                        mRID: me.mRID
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('devicechannels.channels', 'MDC', 'Channels'),
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
                        title: Uni.I18n.translate('deviceloadprofilechannels.empty.title', 'MDC', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('deviceloadprofilechannels.empty.list.item1', 'MDC', 'No channels have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileChannelsPreview',
                        router: me.router,
                        device: me.device
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});