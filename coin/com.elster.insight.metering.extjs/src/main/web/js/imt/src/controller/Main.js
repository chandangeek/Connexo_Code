/**
 * @class Imt.controller.Main
 */
Ext.define('Imt.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Imt.usagepointmanagement.controller.View',
        'Imt.usagepointmanagement.controller.Edit',
        'Imt.devicemanagement.controller.Device',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.devicemanagement.model.Device',
        'Imt.channeldata.controller.View',
        'Imt.registerdata.controller.View'
    ],

    privileges: [],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.devicemanagement.model.Device',
        'Imt.channeldata.model.Channel',
        'Imt.registerdata.model.Register'
    ],
    controllers: [
		'Imt.usagepointmanagement.controller.View',
		'Imt.usagepointmanagement.controller.Edit',
		'Imt.devicemanagement.controller.Device',
        'Imt.channeldata.controller.View',
        'Imt.registerdata.controller.View',
        'Imt.metrologyconfiguration.controller.View',
        'Imt.metrologyconfiguration.controller.Edit',
        'Imt.metrologyconfiguration.controller.ViewList',
        'Imt.controller.History',
        'Imt.controller.Search'
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

    initHistorians: function() {
        this.getController('Imt.controller.History');
    },
    
    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.label.usagepoints', 'IMT', 'Usage points'),
            href: 'usagepoints',
            portal: 'usagepoints',
            glyph: 'devices'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.label.administration', 'IMT', 'Administration'),
            portal: 'usagepoints',
            items: [
                {
                    text: Uni.I18n.translate('general.label.usagepoint.add', 'IMT', 'Add usage point'),
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
                    text: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
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
