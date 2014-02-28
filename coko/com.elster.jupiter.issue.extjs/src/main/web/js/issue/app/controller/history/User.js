Ext.define('Mtr.controller.history.User', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'users',

    doConversion: function (tokens) {
        Mtr.getApplication().getUserController().showOverview();
    }
});