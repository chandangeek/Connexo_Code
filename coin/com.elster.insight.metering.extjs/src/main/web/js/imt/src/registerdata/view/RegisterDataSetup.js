Ext.define('Imt.registerdata.view.RegisterDataSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerDataSetup',
    itemId: 'registerDataSetup',
    requires: [
//        'Imt.registerdata.view.RegisterList',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.registerdata.view.RegisterDataList',
        'Imt.registerdata.view.RegisterTopFilter'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        mRID: me.mRID,
                    }
                ]
            }
        ];
        
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'registerDataSetupPanel',
                layout: {
                    type: 'fit',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    }
                },       

                items: [{
                    xtype: 'preview-container',    
                    grid: {
                        xtype: 'registerDataList',
                        router: me.router,
                        mRID: me.mRID,
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-device-register-config',
                        title: Uni.I18n.translate('deviceregisterconfiguration.empty.title', 'IMT', 'No registers found'),
                        reasons: [
                            Uni.I18n.translate('deviceregisterconfiguration.empty.list.item1', 'IMT', 'No registers have been defined yet.')
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
            }
        ];
        me.callParent(arguments);
    }
});