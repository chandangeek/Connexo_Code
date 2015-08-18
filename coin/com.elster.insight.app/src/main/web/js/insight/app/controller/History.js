Ext.define('InsightApp.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'INS', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
            	usagepointadd: {
                	title: Uni.I18n.translate('general.usagePointAdd', 'INS', 'Add Usage Point'),
                    route: 'usagepoints/add',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'createUsagePoint',
            	},
           		usagepoint: {
           			title: Uni.I18n.translate('general.usagePointView', 'INS', 'View Usage Point'),
           			route: 'usagepoints/{mRID}',
           			controller: 'Imt.usagepointmanagement.controller.View',
           			action: 'showUsagePoint'
           		},
   				usagepointedit: {
                	title: Uni.I18n.translate('general.usagePointEdit', 'INS', 'Edit Usage Point'),
                    route: 'usagepoints/{mRID}/edit',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'               					
   				},
   				device: {
           			title: Uni.I18n.translate('general.device.view', 'INS', 'View Device'),
           			route: 'devices/{mRID}',
           			controller: 'Imt.devicemanagement.controller.Device',
           			action: 'showDevice'
           		}
            }
        }
    }
});
