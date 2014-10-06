Ext.define('Sam.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
        'Sam.controller.history.Administration',
        'Sam.controller.licensing.Licenses',
        'Sam.controller.licensing.Upload'
    ],

    stores: [
        'Sam.store.Licensing'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this;

        if (Uni.Auth.hasAnyPrivilege(['privilege.upload.license','privilege.view.license'])) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'SAM', 'Administration'),
                href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });
            Uni.store.MenuItems.add(menuItem);

            if (Uni.Auth.hasAnyPrivilege(['privilege.upload.license','privilege.view.license'])) {
                var licensingItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.licenses', 'SAM', 'Licensing'),
                    portal: 'administration',
                    route: 'licensing',
                    items: [
                        {
                            text: Uni.I18n.translate('general.licenses', 'MDC', 'Licenses'),
                            href: '#/administration/licensing/licenses',
                            route: 'licenses'
                        }
                    ]
                });

                Uni.store.PortalItems.add(
                    licensingItem
                );
            }
        }
    }
});

