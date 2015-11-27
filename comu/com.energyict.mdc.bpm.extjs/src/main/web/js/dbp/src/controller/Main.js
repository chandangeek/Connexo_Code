Ext.define('Dbp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Dbp.processes.controller.Processes'
    ],

    controllers: [
        'Dbp.controller.History',
        'Dbp.deviceprocesses.controller.DeviceProcesses',
        'Dbp.deviceprocesses.controller.StartProcess',
        'Dbp.processes.controller.Processes'
    ],

    stores: [
        'Dbp.processes.store.Processes'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Dbp.controller.History'); // Forces route registration.

        me.getApplication().fireEvent('cfginitialized');

        me.addProcessManagement();
    },

    addProcessManagement: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null;

        if (Dbp.privileges.DeviceProcesses.canViewProcesses()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'DBP', 'Administration'),
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));
        }

        if (Dbp.privileges.DeviceProcesses.canViewProcesses()) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.processManagement', 'DBP', 'Process management'),
                portal: 'administration',
                route: 'managementprocesses',
                items: [
                    {
                        text: Uni.I18n.translate('general.managementprocesses.processes', 'BPM', 'Processes'),
                        itemId: 'processes',
                        href: router.getRoute('administration/managementprocesses').buildUrl()
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    },
});