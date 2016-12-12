Ext.define('Imt.metrologyconfiguration.view.validation.ValidationConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-validation-tab-view',

    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.validation.ValidationRuleSets',
        'Imt.metrologyconfiguration.view.validation.ValidationSchedule'
    ],

    router: null,
    metrologyConfig: null,
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
            activeTab: 'metrology-configuration-' + me.activeTab,
            items: [
                {
                    title: Uni.I18n.translate('general.ruleSets', 'IMT', 'Rule sets'),
                    itemId: 'metrology-configuration-rules',
                    metrologyConfig: me.metrologyConfig,
                    listeners: {
                        activate: me.controller.showRulesTab,
                        scope: me.controller
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
