Ext.define('Mtr.controller.history.Party', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'parties',

    doConversion: function (tokens) {
        Mtr.getApplication().getPartyController().showOverview();
    }
});