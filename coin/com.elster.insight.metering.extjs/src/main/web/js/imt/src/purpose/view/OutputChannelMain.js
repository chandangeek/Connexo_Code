Ext.define('Imt.purpose.view.OutputChannelMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.output-channel-main',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Imt.purpose.view.OutputSpecificationsForm',
        'Imt.purpose.view.OutputReadings'
    ],

    initComponent: function () {
        var me = this,
            router= me.router;

        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: router.getRoute().getTitle(),
                itemId: 'channelTabPanel',
                activeTab: 'output-' + me.tab,
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'MDC', 'Specifications'),
                        itemId: 'output-specifications',
                        items: {
                            xtype: 'output-specifications-form',
                            router: me.router
                        },
                        listeners: {
                            activate: me.controller.showSpecificationsTab,
                            scope: me.controller
                        }
                    },
                    {
                        title:  Uni.I18n.translate('deviceloadprofiles.readings', 'MDC', 'Readings'),
                        itemId: 'output-readings',
                        items: {
                            xtype: 'output-readings',
                            interval: me.interval,
                            purpose: me.purpose,
                            output: me.output,
                            router: me.router
                        },
                        listeners: {
                            activate: me.controller.showReadingsTab,
                            scope: me.controller
                        },
                        usagePoint: me.usagePoint,
                        purpose: me.purpose,
                        output: me.output

                    }
                ],
                tabBar: {
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'previous-next-navigation-toolbar',
                            itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                            store: 'Imt.purpose.store.Outputs',
                            router: me.router,
                            routerIdArgument: 'outputId',
                            itemsName: me.prevNextListLink,
                            indexLocation: 'arguments',
                            isFullTotalCount: true
                        }
                    ]
                }
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                style: {
                    paddingRight: 0
                },
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
