Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        Mdc.getApplication().getSetupController().showOverview();
    }
});