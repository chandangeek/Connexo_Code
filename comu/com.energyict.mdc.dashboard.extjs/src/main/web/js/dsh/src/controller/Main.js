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
        'Dsh.util.FilterStoreHydrator'
    ],

    controllers: [
        'Dsh.controller.history.Workspace',
        'Dsh.controller.BaseController',
        'Dsh.controller.CommunicationOverview',
        'Dsh.controller.ConnectionOverview',
        'Dsh.controller.Connections',
        'Dsh.controller.Communications'
    ],

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType',
        'Dsh.store.filter.CompletionCodes',
        'Dsh.store.filter.DeviceGroup',
        'Dsh.store.ConnectionResultsStore',
        'Dsh.store.CommunicationResultsStore',
        'Dsh.store.CombineStore'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Dsh.controller.history.Workspace'); // Forces route registration.

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.communicationInfrastructure'])) {
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
                            text: Uni.I18n.translate('title.connections', 'DSH', 'Connections'),
                            href: router.getRoute('workspace/connections/details').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.connections.overview', 'DSH', 'Connections overview'),
                            href: router.getRoute('workspace/connections').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.communications', 'DSH', 'Communications'),
                            href: router.getRoute('workspace/communications/details').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.communications.overview', 'DSH', 'Communications overview'),
                            href: router.getRoute('workspace/communications').buildUrl()
                        }
                    ]
                })
            );
        }
    }
});