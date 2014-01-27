Ext.define('Cfg.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    doConversion: function (tokens) {
        this.showOverview();
    },

    showOverview: function () {
        Cfg.getApplication().getAdministrationController().showOverview();
    }
});
