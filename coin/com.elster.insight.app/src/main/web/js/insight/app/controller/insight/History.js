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
                	title: Uni.I18n.translate('general.usagepoints', 'INS', 'View Usage Points'),
                    route: 'usagepoints',
                   	controller: 'Mtr.usagepointmanagement.controller.View',
                   	action: 'showUsagePoints'               	
                },
            	add: {
                	title: Uni.I18n.translate('general.usagePointAdd', 'INS', 'Add Usage Point'),
                    route: 'usagepoints/add',
                    controller: 'Mtr.usagepointmanagement.controller.Edit',
                    action: 'createUsagePoint',
            	},
           		usagepoint: {
           			title: Uni.I18n.translate('general.usagePointView', 'INS', 'View Usage Point'),
           			route: 'usagepoints/{mRID}',
           			controller: 'Mtr.usagepointmanagement.controller.View',
           			action: 'showUsagePoint'
           		},
   				edit: {
                	title: Uni.I18n.translate('general.usagePointEdit', 'INS', 'Edit Usage Point'),
                    route: 'usagepoints/{mRID}/edit',
                    controller: 'Mtr.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'               					
   				}
            }
        }
    }
});
