/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo System Admin',
    defaultToken: '#/administration',
    searchEnabled: false,
    privileges: ['privilege.administrate.userAndRole','privilege.view.userAndRole','privilege.view.license','privilege.upload.license',
                'privilege.administrate.period','privilege.view.period',
                'privilege.administrate.dataExportTask','privilege.view.dataExportTask','privilege.update.dataExportTask','privilege.update.schedule.dataExportTask','privilege.run.dataExportTask'],
    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
        'Dxp.controller.Main'
    ]
});
