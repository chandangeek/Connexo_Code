Ext.define('Tme.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Uni.store.PortalItems'
    ],

    controllers: [
        'Tme.controller.RelativePeriods'
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Tme.controller.history.Time');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        Uni.store.MenuItems.add(menuItem);
    }
});

