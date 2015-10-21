Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigurationListSetup',
    itemId: 'metrologyConfigurationListSetup',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationList',
    ],
    router: null,
    initComponent: function () {
        var me = this;
        
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'metrologyConfigurationListSetupPanel',
                title: Uni.I18n.translate('metrologyConfiguration.metrologyConfigurationList', 'IMT', 'Metrology configurations'),
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
                        xtype: 'metrologyConfigurationList',
//                        router: me.router,
//                        mRID: me.mRID,
//                        registerId: me.registerId
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-metrology-configurations',
                        title: Uni.I18n.translate('metrologyConfigurationList.empty.title', 'IMT', 'No metrology configurations found'),
                        reasons: [
                            Uni.I18n.translate('metrologyConfigurationList.empty.list.item1', 'IMT', 'No metrology configurations have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'container',
                        itemId: 'previewComponentContainer'
                    }
                }],
            }
        ];
        me.callParent(arguments);
    }
});