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
                	title: Uni.I18n.translate('general.usagepoints', 'INS', 'Usage Points'),
                    route: 'usagepoints',
                    controller: 'Mtr.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'
                },
                viewusagepoints: {
                	title: Uni.I18n.translate('general.usagepoints', 'INS', 'View Usage Points'),
                    route: 'viewusagepoints',
                   	controller: 'Mtr.usagepointmanagement.controller.View',
                   	action: 'showUsagePoints',
                   	items: {
                   		usagepoint: {
                   			route: '{mRID}',
                   			controller: 'Mtr.usagepointmanagement.controller.View',
                   			action: 'showUsagePoint'
                   		}
                   	}
                },
            }
        }
    }
});
