Ext.define('Mdc.view.setup.devicechannels.TabbedDeviceChannelsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceChannelsView',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Mdc.view.setup.devicechannels.SideFilter',
        'Mdc.view.setup.devicechannels.Data'
    ],

    router: null,
    channel: null,
    device: null,
    channelsListLink: null,
    activeTab: null,

    setFilterView: function (filterModel) {
        console.log(filterModel);
        var me = this,
            filterView = me.down('#deviceloadprofileschanneldatafilterpanel'),
            filterForm = me.down('#deviceLoadProfileChannelDataFilterForm'),

            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=duration]'),

            suspectField = filterForm.down('#suspect'),
            nonSuspectField = filterForm.down('#nonSuspect'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getRawValue(),
            suspect = suspectField.boxLabel,
            nonSuspect = nonSuspectField.boxLabel,
            eventDateText = '';
        eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
            + Uni.DateTime.formatDateShort(intervalStart);

        filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
        filterView.down('#Reset').setText('Reset');

        if (suspectField.getValue()) {
            filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
        }

        if (nonSuspectField.getValue()) {
            filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
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
//                    {
//                        xtype: 'deviceLoadProfileChannelGraphView'
//                    },
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
                                store: 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
                                router: me.router,
                                routerIdArgument: 'channelId',
                                itemsName: me.channelsListLink
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
                        hidden: true
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});
