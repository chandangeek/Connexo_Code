Ext.define('Imt.registerdata.view.RegisterDataSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerDataSetup',
    itemId: 'registerDataSetup',
    requires: [
//        'Imt.registerdata.view.RegisterList',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
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
                        registerId: me.registerId
                    }
                ]
            }
        ];
        
        me.content = [
              
            {
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
//                        marginRight: '20px',
//                        padding: '20px'
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
                        xtype: 'uni-form-empty-message',
                        itemId: 'ctr-no-device-register-config',
                        text: Uni.I18n.translate('registerdata.label.register.list.undefined', 'IMT', 'No registers have been defined yet.')
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