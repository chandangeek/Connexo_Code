Ext.define('Yfn.controller.history.YellowfinReports', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'reports',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        deviceConfig: {
            title: Uni.I18n.translate('report.deviceconfig.title', 'YFN', 'Device Config'),
            route: 'reports/deviceconfig',
            controller: 'Yfn.controller.YellowfinReportsController',
            privileges: ['privilege.import.inventoryManagement'],
            action: 'showDeviceConfig'
        },
        devicegatewaytopology: {
            title: Uni.I18n.translate('report.devicegatewaytopology.title', 'YFN', 'Device / Gateway topology'),
            route: 'reports/devicegatewaytopology',
            controller: 'Yfn.controller.YellowfinReportsController',
            privileges: ['privilege.import.inventoryManagement'],
            action: 'showDeviceGatewayTopology'
        },
        communicatioperformance: {
            communicatioperformance: Uni.I18n.translate('report.communicationperformance.title', 'YFN', 'Communication (connection) performance'),
            route: 'reports/communicationperformance',
            controller: 'Yfn.controller.YellowfinReportsController',
            privileges: ['privilege.import.inventoryManagement'],
            action: 'showCommunicationPerformance'
        }
    }
});