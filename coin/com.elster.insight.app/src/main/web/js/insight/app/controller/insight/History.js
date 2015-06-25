Ext.define('InsightApp.controller.insight.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'insight',

    routeConfig: {
        insight: {
            title: Uni.I18n.translate('general.insight', 'INS', 'Insight'),
            route: 'insight',
            disabled: true,
            items: {
                properties: {
                    route: 'properties',
                    controller: 'InsightApp.controller.insight.Properties',
                    action: 'test'
                }
            }
        }
    }
});