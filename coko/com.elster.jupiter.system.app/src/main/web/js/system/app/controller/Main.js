/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Tme.privileges.Period',
        'Fim.privileges.DataImport',
        'Apr.privileges.AppServer'
    ],

    applicationTitle: 'Connexo Admin',
    defaultToken: '/administration',
    searchEnabled: false,
    onlineHelpEnabled: true,
    privileges:  Ext.Array.merge(
        Usr.privileges.Users.all(),
        Sam.privileges.DataPurge.all(),
        Sam.privileges.License.all(),
        Tme.privileges.Period.all(),
        Fim.privileges.DataImport.all(),
        Apr.privileges.AppServer.all()
    ),

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
		'Apr.controller.Main',
        'Fim.controller.Main'
    ]
});
