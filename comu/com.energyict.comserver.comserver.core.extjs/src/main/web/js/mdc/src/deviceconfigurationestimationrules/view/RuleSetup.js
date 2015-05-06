Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetup', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.device-configuration-estimation-rules-setup',

    requires: [
        'Mdc.deviceconfigurationestimationrules.view.RulesGrid',
        'Mdc.deviceconfigurationestimationrules.view.Preview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'device-configuration-estimation-rules-grid',
            router: me.router,
            itemId: 'estimationRulesGrid'
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            title: Uni.I18n.translate('deviceconfiguration.estimation.rules.title', 'MDC', 'No estimation rules found'),
            reasons: [
                Uni.I18n.translate('deviceconfiguration.estimation.rules.empty.list.item1', 'MDC', 'No estimation rules have been created yet.')
            ]
        };

        me.previewComponent = {
            xtype: 'device-configuration-estimation-rules-preview',
            router: me.router
        };

        me.callParent(arguments);
    }

});

