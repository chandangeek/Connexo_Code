Ext.define('Mdc.view.setup.deviceloadprofiles.TabbedDeviceLoadProfilesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceLoadProfilesView',
    itemId: 'tabbedDeviceLoadProfilesView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation'
    ],
    router: null,
    title: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'tabpanel',
                title: me.title,
                ui: 'large',
                itemId: 'loadProfileTabPanel',
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'MDC', 'Specifications'),
                        itemId: 'loadProfile-specifications'
                    },
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.readings', 'MDC', 'Readings'),
                        itemId: 'loadProfile-data'
                    }],
                listeners: {
                    afterrender: function(panel){
                        var bar = panel.tabBar;
                        bar.insert(2,[
                            {
                                xtype: 'tbfill'
                            },
                            {
                                xtype: 'previous-next-navigation-toolbar',
                                itemId: 'tabbed-device-loadProfiles-view-previous-next-navigation-toolbar',
                                store: 'Mdc.store.LoadProfilesOfDevice',
                                router: me.router,
                                routerIdArgument: 'loadProfileId',
                                itemsName: '<a href="' + me.router.getRoute('devices/device/loadprofiles').buildUrl() + '">' + Uni.I18n.translate('deviceloadprofiles.loadProfiles', 'MDC', 'Load profiles').toLowerCase() + '</a>'
                            }
                        ]);
                    }
                }
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideLoadProfilesPanel',
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
                                loadProfileId: me.loadProfileId,
                                toggleId: 'loadProfilesLink'
                            }
                        ]
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});
