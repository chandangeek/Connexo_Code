Ext.define('Imt.registerdata.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerListSetup',
    itemId: 'registerListSetup',
    requires: [
        'Imt.registerdata.view.RegisterList',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        mRID: me.mRID
                    }
                ]
            }
        ];
        
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'registerListSetupPanel',
                title: Uni.I18n.translate('registers.registerList', 'IMT', 'Registers'),
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
                    title: Uni.I18n.translate('registers.registerList', 'IMT', 'Registers'),
                    grid: {
                        xtype: 'registerList',
                        mRID: me.mRID,
                        router: me.router
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
                }]
            }
        ];
        me.callParent(arguments);
        me.down('#registerList').setTitle(me.mRID);
    }
});