Ext.define('Usr.controller.history.Home', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'home',

    doConversion: function (tokens) {
        Usr.getApplication().getHomeController().showOverview();
    }
});