Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigValRulesSetSetup',
    itemId: 'metrologyConfigValRulesSetSetup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm',
        'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',
    ],
    router: null,
//    content: [
//        {
//            xtype: 'panel',
//            ui: 'large',
//            itemId: 'metrologyConfigValRulesSetSetupPanel',
//            layout: {
//                type: 'fit',
//                align: 'stretch'
//            }
//        }
//    ],

//    initComponent: function () {
//    	var me = this,
//            panel = me.content[0];
// //       panel.title = me.router.getRoute().getTitle();
//        me.side = [
//            {
//                xtype: 'panel',
//                ui: 'medium',
//                items: [
//                    {
//                        xtype: 'metrology-configuration-side-menu',
//                        itemId: 'metrology-configuration-side-menu',
//                        router: me.router,
//                        mcid: me.mcid
//                    }
//                ]
//            }
//        ];
//        this.callParent(arguments);
//
//        me.down('#metrologyConfigValRulesSetSetupPanel').add(
//            {
//                xtype: 'panel',
//                layout: {
//                    type: 'hbox'
//                },
//                defaults: {
//                    style: {
//                        marginRight: '20px',
//                        padding: '20px'
//                    },
//                    flex: 1
//                },
//                items: [
//                    {
//                        xtype: 'panel',
//                        title: Uni.I18n.translate('metrologyConfiguration.valRulesSet', 'IMT', 'Metrology Configuration Validation Rule Sets'),
//                        ui: 'tile',
//                        itemId: 'metrology-config-val-rules-set-panel',
//                        router: me.router,
//                    }
//                ]
//            }
//        );
//    }
    
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: me.router,
                        mcid: me.mcid,
                    }
                ]
            }
        ];
        
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'MetrologyConfigValRulesSetSetupPanel',
                title: Uni.I18n.translate('metrologyconfiguration.managevalrulesets', 'IMT', 'Validation Rule Sets'),
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
                        xtype: 'metrologyConfigValRulesSetEditPanel',
                        router: me.router,
                        mcid: me.mcid,
                    },
                }],
            }
        ];
        me.callParent(arguments);
    }
    
    
    
});