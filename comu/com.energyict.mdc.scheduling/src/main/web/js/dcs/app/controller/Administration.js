Ext.define('Dcs.controller.Administration', {
    extend: 'Ext.app.Controller',

    stores: [
    ],

    views: [
        'admin.Administration'
    ],


    showOverview: function () {
        var widget = Ext.create('Dcs.view.admin.Administration');
        this.getApplication().getController('Dcs.controller.Main').showContent(widget);
    }

});
