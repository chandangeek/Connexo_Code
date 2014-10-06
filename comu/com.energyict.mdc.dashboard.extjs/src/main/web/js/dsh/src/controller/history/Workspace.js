//TODO: localize all strings
Ext.define('Dsh.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                connections: {
                    title: 'Connections overview',
                    route: 'connections',
                    controller: 'Dsh.controller.ConnectionOverview',
                    action: 'showOverview',
                    items: {
                        details: {
                            title: 'Connections',
                            route: 'details',
                            controller: 'Dsh.controller.Connections',
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                },
                communications: {
                    title: 'Communication overview',
                    route: 'communications',
                    controller: 'Dsh.controller.CommunicationOverview',
                    action: 'showOverview',
                    items: {
                        details: {
                            title: 'Communications',
                            route: 'details',
                            controller: 'Dsh.controller.Communications',
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                }
            }
        }
    }
});
//'workspace/datacommunication/communication' --> workspace/communications
//'workspace/datacommunication/communications' --> workspace/communications/details
//'workspace/datacommunication/connection' --> workspace/connections
//'workspace/datacommunication/connections' --> workspace/connections/details