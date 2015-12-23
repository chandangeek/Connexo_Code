Ext.define('Imt.registerdata.view.RegisterTabbedView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTabbedView',
    itemId: 'registerTabbedView',
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
                        title: Uni.I18n.translate('registerdata.specifications', 'IMT', 'Specifications'),
                        itemId: 'register-specifications'
                    },
                    {
                        title: Uni.I18n.translate('registerdata.readings', 'IMT', 'Readings'),
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
                                itemId: 'tabbed-usagepoint-register-view-previous-next-navigation-toolbar',
                                store: 'Register',
                                router: me.router,
                                routerIdArgument: 'registerId',
                                itemsName: '<a href="' + me.router.getRoute('usagepoints/registers').buildUrl() + '">' + Uni.I18n.translate('general.registers', 'IMT', 'Registers').toLowerCase() + '</a>'
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
                                xtype: 'usage-point-side-menu',
                                itemId: 'stepsMenu',
                                usagepoint: me.usagepoint,
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
