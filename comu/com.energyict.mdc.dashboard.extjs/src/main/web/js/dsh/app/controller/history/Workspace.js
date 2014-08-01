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
                datacommunication: {
                    title: 'Data communication',
                    route: 'datacommunication',
                    items: {
                        communication: {
                            title: 'Communication overview',
                            route: 'communication',
                            controller: 'Dsh.controller.ConnectionOverview',
                            action: 'showOverview'
                        },
                        communications: {
                            title: 'Communications',
                            route: 'communications',
                            controller: 'Dsh.controller.ConnectionOverview',
                            action: 'showOverview'
                        },
                        connection: {
                            title: 'Connection overview',
                            route: 'connection',
                            controller: 'Dsh.controller.ConnectionOverview',
                            action: 'showOverview'
                        },
                        connections: {
                            title: 'Connections',
                            route: 'connections',
                            controller: 'Dsh.controller.ConnectionOverview',
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});