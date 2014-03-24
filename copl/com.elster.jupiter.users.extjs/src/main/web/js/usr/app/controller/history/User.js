Ext.define('Usr.controller.history.User', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'users',

    doConversion: function (tokens) {
        Usr.getApplication().getUserController().showOverview();
    }
});