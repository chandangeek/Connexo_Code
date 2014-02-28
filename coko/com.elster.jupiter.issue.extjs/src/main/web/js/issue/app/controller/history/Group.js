Ext.define('Mtr.controller.history.Group', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'groups',

    doConversion: function (tokens) {
        Mtr.getApplication().getGroupController().showOverview();
    }
});