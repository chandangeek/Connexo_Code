Ext.define('Mdc.controller.Setup', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.Browse'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Setup',
            href: Mdc.getApplication().getHistorySetupController().tokenizeShowOverview(),
            glyph: 'xe01d@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('setupBrowse');
        Mdc.getApplication().getMainController().showContent(widget);
    }

});
