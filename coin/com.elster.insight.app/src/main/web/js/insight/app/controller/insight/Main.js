Ext.define('InsightApp.controller.insight.Main', {
    extend: 'Ext.app.Controller',

    controllers: [
        'InsightApp.controller.insight.History',
 //       'InsightApp.controller.insight.UsagePoint',
        'InsightApp.usagepointmanagement.controller.UsagePoint'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
        this.callParent();
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        this.getController('InsightApp.controller.insight.History');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.insight', 'INS', 'Insight'),
            href: 'insight',
            portal: 'insight',
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.UsagePoints', 'INS', 'Usage Points'),
            portal: 'insight',
            items: [
                {
                    text: Uni.I18n.translate('general.usagePointAdd', 'INS', 'Add Usage Point'),
                    href: '#/insight/usagepoints',
                    itemId: 'add-usagepoints'
                },
                {
                    text: Uni.I18n.translate('general.usagePointView', 'INS', 'View Usage Point'),
                    href: '#/insight/viewusagepoints',
                    itemId: 'view-usagepoints'
                },
            ]
        });

        Uni.store.PortalItems.add(
            portalItem1
        );
    }
});