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
        'Dal.controller.Detail',
        //    'Isu.controller.MessageWindow'
    ],

    stores: [
        //    'Isu.store.Issues'
    ],

    refs: [
        /*    {
         ref: 'viewport',
         selector: 'viewport'
         },
         {
         ref: 'contentPanel',
         selector: 'viewport > #contentPanel'
         }
         */],

    init: function () {
        this.initMenu();
        //     this.getApplication().fireEvent('initIssueType', 'datacollection');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            alarms = null,
            historian = me.getController('Dal.controller.history.Workspace'); // Forces route registration.

        //if (Isu.privileges.Issue.canViewAdminDevice()) {
        Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.workspace', 'DAL', 'Workspace'),
            glyph: 'workspace',
            portal: 'workspace',
            index: 30
        }));
        //}

        //if (Isu.privileges.Issue.canViewAdminDevice()) {
        alarms = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.alarms', 'DAL', 'Alarms'),
            portal: 'workspace',
            route: 'alarms',
            items: [
                {
                    text: Uni.I18n.translate('device.alarms', 'DAL', 'Alarms'),
                    href: router.getRoute('workspace/alarms').buildUrl({})
                }
            ]
        });
        //}

        if (alarms !== null) {
            Uni.store.PortalItems.add(alarms);
        }
    }
});