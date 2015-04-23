/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Yfn.privileges.Yellowfin',
        'Dxp.privileges.DataExport',
        'Tme.privileges.Period'
    ],

    applicationTitle: 'Connexo Admin',
    defaultToken: '/administration',
    searchEnabled: false,
    onlineHelpEnabled: true,
    privileges:  Ext.Array.merge(
        Usr.privileges.Users.any(),
        Sam.privileges.DataPurge.any(),
        Sam.privileges.License.any(),
        Yfn.privileges.Yellowfin.any(),
        Dxp.privileges.DataExport.any(),
        Tme.privileges.Period.any()),

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
        'Dxp.controller.Main',
		'Apr.controller.Main'
    ]
});
