//TODO: localize all strings
Ext.define('Dsh.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    requires: [
        'Dsh.model.OverviewFilter',
        'Dsh.model.Filter',
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceGroup'
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
                    privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.connections', 'DSH', 'Connections'),
                            route: 'details',
                            controller: 'Dsh.controller.Connections',
                            privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter',
                            items: {
                                bulk: {
                                    title: Uni.I18n.translate('title.bulkAction', 'DSH', 'Bulk action'),
                                    route: 'bulk',
                                    controller: 'Dsh.controller.ConnectionsBulk',
                                    privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                                    action: 'showOverview',
                                    filter: 'Dsh.model.Filter'
                                }
                            }
                        }
                    }
                },
                communications: {
                    title: Uni.I18n.translate('title.communications.overview', 'DSH', 'Communications overview'),
                    route: 'communications',
                    controller: 'Dsh.controller.CommunicationOverview',
                    privileges: ['privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication','privilege.view.device'],
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.communications', 'DSH', 'Communications'),
                            route: 'details',
                            controller: 'Dsh.controller.Communications',
                            privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter',
                            items: {
                                bulk: {
                                    title: Uni.I18n.translate('title.bulkAction', 'DSH', 'Bulk action'),
                                    route: 'bulk',
                                    controller: 'Dsh.controller.CommunicationsBulk',
                                    privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                                    action: 'showOverview',
                                    filter: 'Dsh.model.Filter'
                                }
                            }
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
            filter: 'Dsh.model.OverviewFilter',
            items: {
                selectfavoritedevicegroups: {
                    title: Uni.I18n.translate('title.selectFavoriteDeviceGroups', 'DSH', 'Select favorite device groups'),
                    route: 'selectfavoritedevicegroups',
                    controller: 'Dsh.controller.OperatorDashboard',
                    //privileges: ['privilege.administrate.deviceGroup'],
                    action: 'showMyFavoriteDeviceGroups'
                }
            }
        }
    }
});
