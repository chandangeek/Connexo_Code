Ext.define('Dbp.controller.History', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,
    requires: [],
    routeConfig: {
        'devices/device/processes': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processesrunning': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/running',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            action: 'showDeviceProcesses'
        },
        'devices/device/processeshistory': {
            title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
            route: 'devices/{mRID}/processes/history',
            controller: 'Dbp.deviceprocesses.controller.DeviceProcesses',
            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
            filter: 'Dbp.deviceprocesses.model.HistoryProcessesFilter',
            action: 'showDeviceProcesses'
        }
    }

});

