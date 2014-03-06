Ext.define('Cfg.controller.Administration', {
    extend: 'Ext.app.Controller',

    stores: [
    ],

    views: [
        'admin.Administration'
    ],


    showOverview: function () {
        var widget = Ext.create('Cfg.view.admin.Administration');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    }

});
