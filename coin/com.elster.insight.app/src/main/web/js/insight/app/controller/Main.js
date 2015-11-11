/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Cfg.controller.Main',
        'Uni.controller.Navigation',
        'Imt.controller.Main',
        'InsightApp.controller.History'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/usagepoints',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: [],

    controllers: [
        'Cfg.controller.Main',
        'Imt.controller.Main',
        'InsightApp.controller.History'
    ],
    init: function () {
        // Init historians
        var hist = this.getController('InsightApp.controller.History');
        var router = this.getController('Uni.controller.history.Router');
        this.initMenu();
        
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(1, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.label.usagepoints', 'INS', 'Usage points'),
            href: 'usagepoints',
            portal: 'usagepoints',
            glyph: 'devices'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.label.administration', 'INS', 'Administration'),
            portal: 'usagepoints',
            items: [
                {
                    text: Uni.I18n.translate('general.label.usagepoint.add', 'INS', 'Add usage point'),
                    href: '#/usagepoints/add',
                    itemId: 'add-usagepoints'
                },
            ]
        });

        Uni.store.PortalItems.add(
            portalItem1
        );
        
        var portalItem2 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.label.metrologyconfiguration', 'INS', 'Metrology configuration'),
            portal: 'administration',
            items: [
                {
                    text: Uni.I18n.translate('general.label.metrologyconfiguration', 'INS', 'Metrology configuration'),
                    href: '#/administration/metrologyconfiguration',
                    itemId: 'overview-metrologyconfiguration'
                },
            ]
        });


        Uni.store.PortalItems.add(
            portalItem2
        );
    }
});
