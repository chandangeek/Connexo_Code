Ext.define('InsightApp.controller.insight.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'insight',

    routeConfig: {
        insight: {
            title: Uni.I18n.translate('general.insight', 'INS', 'Insight'),
            route: 'insight',
            disabled: true,
            items: {
                usagepoints: {
                	title: Uni.I18n.translate('general.usagePoints', 'INS', 'Usage Points'),
                    route: 'usagepoints',
                    controller: 'InsightApp.controller.insight.UsagePoint',
                    action: 'test'
                }
            }
        }
    }
});