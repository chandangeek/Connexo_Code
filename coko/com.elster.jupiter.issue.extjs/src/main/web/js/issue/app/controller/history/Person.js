Ext.define('Mtr.controller.history.Person', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'persons',

    doConversion: function (tokens) {
        Mtr.getApplication().getPersonController().showOverview();
    }
});