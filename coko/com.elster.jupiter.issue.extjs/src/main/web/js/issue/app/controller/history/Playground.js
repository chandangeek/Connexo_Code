Ext.define('Mtr.controller.history.Playground', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'playground',

    doConversion: function (tokens) {
        this.showOverview();
    },

    showOverview: function () {
        Mtr.getApplication().getPlaygroundController().showOverview();
    }
});