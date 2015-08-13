/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Uni.controller.Navigation',
        'Mtr.controller.Main',
        'InsightApp.controller.History'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/administration',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: [],

    controllers: [
        'Cfg.controller.Main',
        'Mtr.controller.Main',
        'InsightApp.controller.History'
    ],
    init: function () {
        
        var router = this.getController('Uni.controller.history.Router');
        this.initMenu();
        
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'INS', 'Administration'),
            href: 'administration',
            portal: 'administration',
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.UsagePoints', 'MTR', 'Usage Points'),
            portal: 'administration',
            items: [
                {
                    text: Uni.I18n.translate('general.usagePointAdd', 'MTR', 'Add Usage Point'),
                    href: '#/administration/usagepoints/add',
                    itemId: 'add-usagepoints'
                },
                {
                    text: Uni.I18n.translate('general.usagePointView', 'MTR', 'View Usage Point'),
                    href: '#/administration/usagepoints',
                    itemId: 'view-usagepoints'
                },
                {
                    text: Uni.I18n.translate('general.deviceView', 'MTR', 'View Device'),
                    href: '#/administration/devices',
                    itemId: 'view-devices'
                }
            ]
        });

        Uni.store.PortalItems.add(
            portalItem1
        );
    }
});
