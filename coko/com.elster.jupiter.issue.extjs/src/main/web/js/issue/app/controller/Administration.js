Ext.define('Isu.controller.Administration', {
    extend: 'Ext.app.Controller',

    views: [
        'administration.Overview'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            href: '#/administration',
            glyph: 'xe011@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('administration-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});