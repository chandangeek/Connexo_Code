Ext.define('Mdc.view.setup.deviceregisterconfiguration.TabbedDeviceRegisterView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceRegisterView',
    itemId: 'tabbedDeviceRegisterView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                itemId: 'registerTabPanel',
                items: [
                    {
                        title: Uni.I18n.translate('deviceregisterconfiguration.specifications', 'MDC', 'Specifications'),
                        itemId: 'register-specifications'
                    },
                    {
                        title: Uni.I18n.translate('deviceregisterconfiguration.readings', 'MDC', 'Readings'),
                        itemId: 'register-data'
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
                                itemId: 'tabbed-device-register-view-previous-next-navigation-toolbar',
                                store: 'RegisterConfigsOfDevice',
                                router: me.router,
                                routerIdArgument: 'registerId',
                                itemsName: '<a href="' + me.router.getRoute('devices/device/registers').buildUrl() + '">' + Uni.I18n.translate('general.registers', 'MDC', 'Registers').toLowerCase() + '</a>'
                            }
                        ]);
                    }
                }
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideRegisterPanel',
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
                                registerId: me.registerId,
                                toggleId: 'registersLink'
                            }
                        ]
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});
