Ext.define('Dal.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Dal.privileges.Alarm'
    ],

    controllers: [
        'Dal.controller.history.Workspace',
        'Dal.controller.Alarms',
        'Dal.controller.Detail'
    ],

    stores: [
    ],

    refs: [],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            alarms = null,
            historian = me.getController('Dal.controller.history.Workspace'); // Forces route registration.

        if (Dal.privileges.Alarm.canViewAdminDevice()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'DAL', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Dal.privileges.Alarm.canViewAdminDevice()) {
            alarms = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.alarms', 'DAL', 'Alarms'),
                portal: 'workspace',
                route: 'alarms',
                items: [
                    {
                        itemId: 'alarms-item',
                        text: Uni.I18n.translate('device.alarms', 'DAL', 'Alarms'),
                        href: router.getRoute('workspace/alarms').buildUrl({})
                    }
                ]
            });
        }

        if (alarms !== null) {
            Uni.store.PortalItems.add(alarms);
        }
    }
});