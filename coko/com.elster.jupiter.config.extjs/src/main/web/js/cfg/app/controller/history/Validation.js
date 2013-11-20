Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'validation',

    doConversion: function (tokens) {
        this.showOverview();
    },

    showOverview: function () {
        Cfg.getApplication().getValidationController().showOverview();
    }
});