/**
 * @class YelowfinApp.controller.Main
 */
Ext.define('YellowfinApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    applicationTitle: 'Reports',
    privileges: [],
    defaultToken:"#/error/launch",
    controllers: [],

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);


    }
});
