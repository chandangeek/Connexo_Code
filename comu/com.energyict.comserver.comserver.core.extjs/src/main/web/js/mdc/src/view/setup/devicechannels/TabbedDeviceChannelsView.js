Ext.define('Mdc.view.setup.devicechannels.TabbedDeviceChannelsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceChannelsView',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Mdc.view.setup.devicechannels.SideFilter',
        'Mdc.view.setup.devicechannels.TableView',
        'Mdc.view.setup.devicechannels.GraphView'
    ],

    prevNextstore: null,
    routerIdArgument: null,
    isFullTotalCount: false,

    router: null,
    channel: null,
    device: null,
    prevNextListLink: null,
    activeTab: null,
    indexLocation: null,
    contentName: null,

    setFilterView: function (filter, durationsStore) {
        var me = this,
            filterView = me.down('#deviceloadprofileschanneldatafilterpanel'),
            intervalStart = filter.get('intervalStart'),
            intervalEnd = durationsStore.getById(filter.get('duration')).get('localizeValue'),
            suspect = Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
            nonSuspect = Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
            eventDateText = intervalEnd + ' ' + Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From').toLowerCase() + ' '
                + Uni.DateTime.formatDateShort(intervalStart);
        filterView.setFilter('eventDateChanged', Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'), eventDateText, true);
        filterView.down('#Reset').setText('Reset');
        if (filter.get('onlySuspect')) {
            filterView.setFilter('onlySuspect', Uni.I18n.translate('deviceregisterconfiguration.validation.result', 'MDC', 'Validation result'), suspect);
        }
        if (filter.get('onlyNonSuspect')) {
            filterView.setFilter('onlyNonSuspect', Uni.I18n.translate('deviceregisterconfiguration.validation.result', 'MDC', 'Validation result'), nonSuspect);
        }
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                itemId: 'channelTabPanel',
                activeTab: me.activeTab,
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'MDC', 'Specifications'),
                        itemId: 'channel-specifications',
                        items: {
                            xtype: 'deviceLoadProfileChannelOverview',
                            router: me.router,
                            device: me.device
                        }
                    },
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.readings', 'MDC', 'Readings'),
                        itemId: 'deviceLoadProfileChannelData',
                        items: [
                            {
                                xtype: 'filter-top-panel',
                                itemId: 'deviceloadprofileschanneldatafilterpanel',
                                emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                            },
                            {
                                xtype: 'deviceLoadProfileChannelGraphView'
                            },
                            {
                                xtype: 'deviceLoadProfileChannelTableView',
                                channel: me.channel,
                                router: me.router
                            }
                        ]
                    }
                ],
                listeners: {
                    afterrender: function (panel) {
                        var bar = panel.tabBar;
                        bar.insert(2, [
                            {
                                xtype: 'tbfill'
                            },
                            {
                                xtype: 'previous-next-navigation-toolbar',
                                itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                                store: me.prevNextstore,
                                router: me.router,
                                routerIdArgument: me.routerIdArgument,
                                itemsName: me.prevNextListLink,
                                indexLocation: me.indexLocation,
                                isFullTotalCount: me.isFullTotalCount
                            }
                        ]);
                    }
                }
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideChannelsPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'deviceMenu',
                                itemId: 'stepsMenu',
                                device: me.device,
                                channelId: me.channelId,
                                toggleId: 'channelsLink'
                            }
                        ]
                    },
                    {
                        xtype: 'deviceLoadProfileChannelDataSideFilter',
                        contentName: me.contentName,
                        hidden: (me.activeTab === 0)
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});
