/**
 * @class YelowfinApp.controller.Main
 */
Ext.define('YellowfinApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    applicationTitle: 'Connexo Facts',
    privileges: [],
    defaultToken:"#/error/launch",
    controllers: [
        'Yfn.controller.YellowfinReportsController',
        'Yfn.controller.history.YellowfinReports'],

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });
        var me = this,
            historian = me.getController('Yfn.controller.history.YellowfinReports'); // Forces route registration.


        this.callParent(arguments);


    }
});
