Ext.define('Login.Application', {
    name: 'Login',
    extend: 'Ext.app.Application',

    requires: [
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Login.controller.Base64',
        'Login.controller.Login'
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