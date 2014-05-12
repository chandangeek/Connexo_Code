Ext.define('Usr.Application', {
    name: 'Usr',

    extend: 'Ext.app.Application',

    requires: [
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Usr.controller.Main',
        'Usr.controller.Home',
        'Usr.controller.User',
        'Usr.controller.UserGroups',
        'Usr.controller.Group',
        'Usr.controller.GroupPrivileges',
        'Usr.controller.history.Group',
        'Usr.controller.history.User',
        'Usr.controller.history.Home'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    init: function () {
        this.callParent(arguments);

    },

    launch: function () {
        this.callParent(arguments);

        this.getApplication().fireEvent('changeapptitleevent', Uni.I18n.translate('user.application', 'USM', 'Jupiter System Administration'));
    }
});