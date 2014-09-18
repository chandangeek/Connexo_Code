Ext.define('Dsh.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Dsh.controller.history.Workspace',
        'Dsh.controller.CommunicationOverview',
        'Dsh.controller.ConnectionOverview',
        'Dsh.controller.Connections',
        'Dsh.controller.Communications'
    ],

    config: {
        navigationController: null,
        configurationController: null
    },

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType',
        'Dsh.store.ConnectionResultsStore',
        'Dsh.store.CombineStore'
    ],

    init: function () {
        this.initNavigation();
        this.initMenu();
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation'),
            configurationController = this.getController('Uni.controller.Configuration');
        this.setNavigationController(navigationController);
        this.setConfigurationController(configurationController);
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Dsh.controller.history.Workspace'); // Forces route registration.

        Uni.store.MenuItems.add(
            Ext.create('Uni.model.MenuItem', {
                text: 'Workspace',
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            })
        );

        Uni.store.PortalItems.add(
            Ext.create('Uni.model.PortalItem', {
                title: 'Data communication',
                portal: 'workspace',
                route: 'datacommunication',
                items: [
                    {
                        text: 'Connections',
                        href: router.getRoute('workspace/datacommunication/connections').buildUrl()
                    },
                    {
                        text: 'Connection overview',
                        href: router.getRoute('workspace/datacommunication/connection').buildUrl()
                    },
                    {
                        text: 'Communications',
                        href: router.getRoute('workspace/datacommunication/communications').buildUrl()
                    },
                    {
                        text: 'Communication overview',
                        href: router.getRoute('workspace/datacommunication/communication').buildUrl()
                    }
                ]
            })
        );
    }
});