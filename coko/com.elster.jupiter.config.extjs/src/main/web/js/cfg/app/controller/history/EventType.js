Ext.define('Cfg.controller.history.EventType', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'eventtypes',

    doConversion: function (tokens) {
        this.showOverview();
    },

    showOverview: function () {
        this.getApplication().getEventTypeController().showOverview();
    }
});
