Ext.define('Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceLogBookView',
    itemId: 'tabbedDeviceLogBookView',

    requires: [
        'Uni.view.toolbar.PreviousNextNavigation'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'tabpanel',
                itemId: 'logBookTabPanel',
                ui: 'large',
                items: [
                    {
                        title: Uni.I18n.translate('devicelogbooks.specification', 'MDC', 'Specifications'),
                        itemId: 'logBook-specifications'
                    },
                    {
                        title: Uni.I18n.translate('general.events', 'MDC', 'Events'),
                        itemId: 'logBook-data'
                    }
                ],
                listeners: {
                    afterrender: function(panel){
                        var bar = panel.tabBar;
                        bar.insert(2,[
                            {
                                xtype: 'tbfill'
                            },
                            {
                                xtype: 'previous-next-navigation-toolbar',
                                itemId: 'tabbed-device-logbook-view-previous-next-navigation-toolbar',
                                store: 'Mdc.store.LogbooksOfDevice',
                                router: me.router,
                                routerIdArgument: 'logbookId',
                                itemsName: '<a href="' + me.router.getRoute('devices/device/logbooks').buildUrl() + '">' + Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks').toLowerCase() + '</a>'
                            }
                        ]);
                    }
                }
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideLogBookPanel',
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
                                toggleId: 'logbooksLink'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
