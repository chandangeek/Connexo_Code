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
        'Dsh.controller.Connections'
    ],
    config: {
        navigationController: null,
        configurationController: null
    },
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
            router = me.getController('Uni.controller.history.Router');
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