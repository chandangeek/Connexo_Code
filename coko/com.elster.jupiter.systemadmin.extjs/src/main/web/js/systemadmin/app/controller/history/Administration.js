Ext.define('Sam.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,
    requires: [
        'Sam.controller.Administration'
    ],

    init: function () {
        var me = this;

        crossroads.addRoute('sysadministration', function () {
            me.getController('Sam.controller.Administration').showOverview();
        });

        crossroads.addRoute('administration/licensing/licenses', function () {
            me.getController('Sam.controller.licensing.Licenses').showOverview();
        });
        crossroads.addRoute('administration/licensing/upload', function () {
            me.getController('Sam.controller.licensing.Upload').showOverview();
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens, token) {
        //now has tokens and token (which is you complete path)

        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if (this.currentPath !== null) {
            this.previousPath = this.currentPath;
        }
        this.currentPath = token;
        crossroads.parse(token);
    }
});
