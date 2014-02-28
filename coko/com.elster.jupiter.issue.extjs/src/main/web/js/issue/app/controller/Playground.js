Ext.define('Mtr.controller.Playground', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.window.Wizard'
    ],

    views: [
        'playground.I18n',
        'playground.Wizard'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Playground',
            href: Mtr.getApplication().getHistoryPlaygroundController().tokenizeShowOverview(),
            glyph: 'xe01d@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('playgroundI18n');
        this.getApplication().fireEvent('changecontentevent', widget);

        Ext.create('Mtr.view.playground.Wizard').show();
    }
});