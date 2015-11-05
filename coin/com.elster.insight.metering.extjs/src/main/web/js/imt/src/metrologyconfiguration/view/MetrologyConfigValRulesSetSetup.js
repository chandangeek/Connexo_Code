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