Ext.define('Cfg.controller.Administration', {
    extend: 'Ext.app.Controller',

    stores: [
    ],

    views: [
        'admin.Administration'
    ],


    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        Uni.store.MenuItems.removeAll();

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            href: Cfg.getApplication().getHistoryAdministrationController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        this.initMenu();
        var widget = Ext.create('Cfg.view.admin.Administration');
        Cfg.getApplication().getMainController().showContent(widget);
    }

});
