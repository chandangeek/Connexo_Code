Ext.define('Mtr.controller.history.Dashboard', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'dashboard',

    doConversion: function (tokens) {
        Mtr.getApplication().getDashboardController().showOverview();
    }
});