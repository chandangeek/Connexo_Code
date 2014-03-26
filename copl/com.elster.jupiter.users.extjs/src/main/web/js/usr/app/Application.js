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
        // Removes the loading indicator.
        this.callParent(arguments);

        this.getApplication().fireEvent('changeapptitleevent', Uni.I18n.translate('user.application', 'USM', 'Jupiter System Administration'));
    }
});