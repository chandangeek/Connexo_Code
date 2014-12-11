Ext.define('Mdc.view.setup.deviceloadprofilechannels.TabbedDeviceChannelsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceChannelsView',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.SideFilter'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                itemId: 'channelTabPanel',
                items: [
                    {
                        title: 'Specifications',
                        itemId: 'channel-specifications'
                    },
                    {
                        title: 'Data',
                        itemId: 'channel-data'
                    }],
                listeners: {
                    afterrender: function(panel){
                        var bar = panel.tabBar;
//                        bar.insert(2,[
//                            {
//                                xtype: 'tbfill'
//                            },
//                            {
//                                xtype: 'previous-next-navigation-toolbar',
//                                itemId: 'tabbed-device-register-view-previous-next-navigation-toolbar',
//                                store: 'RegisterConfigsOfDevice',
//                                router: me.router,
//                                routerIdArgument: 'registerId',
//                                itemsName: '<a href="' + me.router.getRoute('devices/device/registers').buildUrl() + '">' + Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers').toLowerCase() + '</a>'
//                            }
//                        ]);
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
