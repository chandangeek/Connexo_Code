Ext.define('Imt.metrologyconfiguration.view.veecommon.ViewConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-vee-tab-view',

    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.estimation.EstimationRuleSets'
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
                    itemId: 'metrology-configuration-estimation-side-menu',
                    router: me.router,
                    metrologyConfig: me.metrologyConfig
                }
            ]
        };

        me.content = {
            xtype: 'tabpanel',
            title: Uni.I18n.translate('usagepoint.dataValidation.estimationConfiguration', 'IMT', 'Estimation configuration'),
            ui: 'large',
            itemId: 'metrology-configuration-estimation-tab-panel',
            activeTab: 'metrology-configuration-' + me.activeTab,
            items: [
                {
                    title: Uni.I18n.translate('general.ruleSets', 'IMT', 'Rule sets'),
                    itemId: 'metrology-configuration-rules',
                    metrologyConfig: me.metrologyConfig,
                    listeners: {
                        activate: me.controller.showEstRulesTab,
                        scope: me.controller
                    }
                },
                // {
                //     title: Uni.I18n.translate('general.schedule', 'IMT', 'Schedule'),
                //     itemId: 'metrology-configuration-schedule',
                //     metrologyConfig: me.metrologyConfig,
                //     listeners: {
                //         activate: me.controller.showScheduleTab,
                //         scope: me.controller
                //     }
                // }
            ]
        };

        me.callParent(arguments);
    }
});
