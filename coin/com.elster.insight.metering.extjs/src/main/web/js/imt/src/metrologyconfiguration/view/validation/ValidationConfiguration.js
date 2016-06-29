Ext.define('Imt.metrologyconfiguration.view.validation.ValidationConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-validation-tab-view',

    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.validation.ValidationRuleSets'
    ],

    router: null,
    metrologyConfig: null,
    purposes: null,
    rulesStore: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'metrology-configuration-side-menu',
                    itemId: 'metrology-configuration-validation-side-menu',
                    router: me.router,
                    metrologyConfig: me.metrologyConfig
                }
            ]
        };

        me.content = {
            xtype: 'tabpanel',
            title: Uni.I18n.translate('usagepoint.dataValidation.validationConfiguration', 'IMT', 'Validation configuration'),
            ui: 'large',
            itemId: 'metrology-configuration-validation-tab-panel',
            activeTab: 0,
            items: [
                {
                    title: Uni.I18n.translate('general.ruleSets', 'IMT', 'Rule sets'),
                    itemId: 'metrology-configuration-validation-rule-sets-tab',
                    items: {
                        xtype: 'validation-mc-rule-sets',
                        metrologyConfig: me.metrologyConfig,
                        purposes: me.purposes,
                        rulesStore: me.rulesStore,
                        router: me.router
                    }                    
                },
                {
                    title: Uni.I18n.translate('general.schedule', 'IMT', 'Schedule'),
                    itemId: 'metrology-configuration-validation-schedule-tab',
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);
    }
});
