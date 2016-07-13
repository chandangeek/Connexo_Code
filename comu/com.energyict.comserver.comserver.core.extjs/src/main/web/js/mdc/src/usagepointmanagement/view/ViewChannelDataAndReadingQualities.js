Ext.define('Mdc.usagepointmanagement.view.ViewChannelDataAndReadingQualities', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.view-channel-data-and-reading-qualities',

    requires: [
        'Uni.grid.FilterPanelTop',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.PreviewContainer',
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.ChannelDataGrid',
        'Mdc.usagepointmanagement.view.ChannelDataPreview'
    ],

    router: null,
    channel: null,
    mRID: null,
    filter: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'view-channel-data-and-reading-qualities-panel',
                title: me.channel.get('readingType').fullAliasName,
                items: [
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                        store: 'Mdc.usagepointmanagement.store.Channels',
                        router: me.router,
                        routerIdArgument: 'channelId',
                        itemsName: Ext.String.format('<a href="{0}">{1}</a>',
                            me.router.getRoute('usagepoints/usagepoint/channels').buildUrl(),
                            Uni.I18n.translate('general.channels', 'MDC', 'Channels').toLowerCase())
                    },
                    {
                        xtype: 'uni-grid-filterpaneltop',
                        itemId: 'channel-data-top-filter',
                        store: 'Mdc.usagepointmanagement.store.ChannelData',
                        hasDefaultFilters: true,
                        filters: [
                            {
                                type: 'duration',
                                text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                                itemId: 'channel-data-top-filter-duration',
                                dataIndex: 'interval',
                                dataIndexFrom: 'intervalStart',
                                dataIndexTo: 'intervalEnd',
                                defaultFromDate: me.filter.defaultFromDate,
                                defaultDuration: me.filter.defaultDuration,
                                durationStore: me.filter.durationStore,
                                loadStore: false
                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'channel-data-grid',
                            itemId: 'channel-data-grid',
                            store: 'Mdc.usagepointmanagement.store.ChannelData',
                            channel: me.channel
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('usagePointChannelData.empty.title', 'MDC', 'No data is available'),
                            reasons: [
                                Uni.I18n.translate('usagePointChannelData.empty.list.item1', 'MDC', 'No data has been collected yet'),
                                Uni.I18n.translate('usagePointChannelData.empty.list.item2', 'MDC', 'No devices have been linked to this usage point in specified period of time')
                            ]
                        },
                        previewComponent: {
                            xtype: 'channel-data-preview',
                            itemId: 'channel-data-preview',
                            router: me.router,
                            channel: me.channel
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