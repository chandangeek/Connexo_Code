Ext.define('Dsh.controller.Main', {
    extend: 'Ext.app.Controller',
    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Dsh.view.Main'
    ],
    controllers: [
        'Dsh.controller.history.Workspace',
        'Dsh.controller.ConnectionOverview'
    ],
    config: {
        navigationController: null,
        configurationController: null
    },
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
        var me = this;

        var workspaceItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            glyph: 'workspace',
            portal: 'workspace',
            index: 30
        });

        Uni.store.MenuItems.add(workspaceItem);

        var router = me.getController('Uni.controller.history.Router');
        var dataCommunication = Ext.create('Uni.model.PortalItem', {
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
        });

        Uni.store.PortalItems.add(
            dataCommunication
        );
    }
});