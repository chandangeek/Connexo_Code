Ext.define('Mdc.usagepointmanagement.view.ViewChannelsList', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.view-channels-list',

    requires: [
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.ChannelsGrid',
        'Mdc.usagepointmanagement.view.ChannelPreview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'view-channels-list-panel',
                title: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'usage-point-channels-grid',
                            itemId: 'usage-point-channels-grid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'usage-point-channels-empty-msg',
                            title: Uni.I18n.translate('usagePointChannels.noItems', 'MDC', 'No available channels found'),
                            reasons: [
                                Uni.I18n.translate('usagePointChannels.empty.list.item1', 'MDC', 'No metrology configuration versions until current moment in time.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('usagePointChannels.stepItems.item1', 'MDC', 'See versions of metrology configurations')
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'usage-point-channel-preview',
                            itemId: 'usage-point-channel-preview',
                            frame: true,
                            router: me.router
                        }
                    }
                ]
            }
        ];

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

        me.callParent(arguments);
    }
});