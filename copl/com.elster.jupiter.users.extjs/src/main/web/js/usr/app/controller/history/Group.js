Ext.define('Usr.controller.history.Group', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'roles',

    doConversion: function (tokens) {
        Usr.getApplication().getGroupController().showOverview();
    }
});