Ext.define('InsightApp.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'INS', 'Administration'),
            route: 'administration',
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
   				},
   				devices: {
   					title: Uni.I18n.translate('general.devices.view', 'INS', 'View Devices'),
   					route: 'devices',
                	controller: 'Mtr.devicemanagement.controller.Device',
                	action: 'showDevices'               	
   				},
   				device: {
           			title: Uni.I18n.translate('general.device.view', 'INS', 'View Device'),
           			route: 'devices/{mRID}',
           			controller: 'Mtr.devicemanagement.controller.Device',
           			action: 'showDevice'
           		}
            }
        }
    }
});
