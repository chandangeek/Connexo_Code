Ext.define('Imt.registerdata.view.RegisterTabbedView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTabbedView',
    itemId: 'registerTabbedView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Imt.registerdata.store.Register',
        'Imt.registerdata.view.Overview'
    ],
    router: null,
    register: null,
    usagepoint: null,
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
                        itemId: 'register-specifications',
                        items: {
                            xtype: 'registerOverview',
                            router: me.router,
                            usagepoint: me.usagepoint
                        }
                    },
                    {
                        title: Uni.I18n.translate('registerdata.readings', 'IMT', 'Readings'),
//                        itemId: 'register-data',
//                        router: me.router,
//                        mRID: me.mRID,
//                        registerId: me.registerId
                        //{
                            xtype: 'panel',
                            ui: 'large',
                            itemId: 'registerDataSetupPanel',
                       //     title: me.registerId, //Uni.I18n.translate('registerdata.label.register.readings', 'IMT', 'Register Readings'),
                            layout: {
                                type: 'fit',
                                align: 'stretch'
                            },
                            defaults: {
                                style: {
//                                    marginRight: '20px',
//                                    padding: '20px'
                                }
                            },       

                            items: [{
                                xtype: 'preview-container',    
                                grid: {
                                    xtype: 'registerDataList',
                                    router: me.router,
                                    mRID: me.mRID,
                                    registerId: me.registerId
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'ctr-no-device-register-config',
                                    title: Uni.I18n.translate('registerdata.label.register.list.empty', 'IMT', 'No registers found'),
                                    reasons: [
                                        Uni.I18n.translate('registerdata.label.register.list.undefined', 'IMT', 'No registers have been defined yet.')
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'container',
                                    itemId: 'previewComponentContainer'
                                }
                            }],
                            dockedItems: [
                                {
                                    dock: 'top',
                                    xtype: 'imt-registerdata-topfilter',
                                    itemId: 'registerdatafilterpanel',
                                    hasDefaultFilters: true,
                                    filterDefault: me.filter
                                }
                             ]
                       // }
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
                                store: 'Imt.registerdata.store.Register',
                                router: me.router,
                                mRID: me.mRID,
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
                            	xtype: 'usage-point-management-side-menu',
                                itemId: 'usage-point-management-side-menu',
                                router: me.router,
                                mRID: me.mRID,
                                toggleId: 'registerLink', 	
                            }
                        ]
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});
