Ext.define('Usr.controller.Home', {
    extend: 'Ext.app.Controller',
    requires: [
    ],
    views: [
        'Home'
    ],

    showOverview: function () {
        var widget = Ext.widget('Home');
        this.getApplication().getController('Usr.controller.Main').showContent(widget);
    }
});