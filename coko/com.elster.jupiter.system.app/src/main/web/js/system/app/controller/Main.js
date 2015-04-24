/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Dxp.privileges.DataExport',
        'Tme.privileges.Period'
    ],

    applicationTitle: 'Connexo Admin',
    defaultToken: '/administration',
    searchEnabled: false,
    onlineHelpEnabled: true,
    privileges:  Ext.Array.merge(
        Usr.privileges.Users.all(),
        Sam.privileges.DataPurge.all(),
        Sam.privileges.License.all(),
        Dxp.privileges.DataExport.all(),
        Tme.privileges.Period.all()
    ),

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
        'Dxp.controller.Main',
		'Apr.controller.Main'
    ]
});
