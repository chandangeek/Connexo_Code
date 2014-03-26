Ext.define('Usr.Application', {
    name: 'Usr',

    extend: 'Ext.app.Application',

    requires: [
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Main',
        'Home',
	    'User',
        'UserGroups',
        'Group',
        'GroupPrivileges',
        //'history.Setup',
        'history.Group',
        'history.User',
        'history.Home'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    init: function () {
        this.callParent(arguments);
    },

    launch: function () {
        this.callParent(arguments);
    }
});