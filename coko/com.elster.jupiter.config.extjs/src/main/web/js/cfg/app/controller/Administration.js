Ext.define('Cfg.controller.Administration', {
    extend: 'Ext.app.Controller',

    stores: [
    ],

    views: [
        'admin.Administration'
    ],


//    init: function () {
//        this.initMenu();
//    },

//    initMenu: function () {
//        Uni.store.MenuItems.removeAll();
//        var me = this;
//        var menuItem = Ext.create('Uni.model.MenuItem', {
//            text: 'Administration',
//            href: me.getApplication().getController('Cfg.controller.history.Administration').tokenizeShowOverview(),
//            glyph: 'xe01e@icomoon'
//        });
//
//        Uni.store.MenuItems.add(menuItem);
//    },

    showOverview: function () {
//        this.initMenu();
        var widget = Ext.create('Cfg.view.admin.Administration');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    }

});
