/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo Admin',
    defaultToken: '/administration',
    searchEnabled: false,
    onlineHelpEnabled: true,
    privileges: ['privilege.administrate.userAndRole','privilege.view.userAndRole','privilege.view.license','privilege.upload.license',
                'privilege.administrate.period','privilege.view.period','privilege.administrate.dataPurge','privilege.view.dataPurge',
                'privilege.administrate.dataExportTask','privilege.view.dataExportTask','privilege.update.dataExportTask',
                'privilege.update.schedule.dataExportTask','privilege.run.dataExportTask','privilege.view.reports','privilege.design.reports'],
    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
		'Apr.controller.Main'
    ]
});
