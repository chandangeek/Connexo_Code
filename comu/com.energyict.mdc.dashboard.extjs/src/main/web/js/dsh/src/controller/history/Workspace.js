//TODO: localize all strings
Ext.define('Dsh.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    requires: [
        'Dsh.model.OverviewFilter',
        'Dsh.model.Filter'
    ],

    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                connections: {
                    title: Uni.I18n.translate('title.connections.overview', 'DSH', 'Connections overview'),
                    route: 'connections',
                    controller: 'Dsh.controller.ConnectionOverview',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.connections', 'DSH', 'Connections'),
                            route: 'details',
                            controller: 'Dsh.controller.Connections',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                },
                communications: {
                    title: Uni.I18n.translate('title.communications.overview', 'DSH', 'Communications overview'),
                    route: 'communications',
                    controller: 'Dsh.controller.CommunicationOverview',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.communications', 'DSH', 'Communications'),
                            route: 'details',
                            controller: 'Dsh.controller.Communications',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                }
            }
        },

        dashboard: {
            title: Uni.I18n.translate('title.dashboard', 'DSH', 'Dashboard'),
            route: 'dashboard',
            controller: 'Dsh.controller.OperatorDashboard',
            action: 'showOverview',
            filter: 'Dsh.model.OverviewFilter'
        }
    }
});
