/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.MonitorProcesses', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    stores: [
    ],
    views: [
        'Mdc.view.setup.monitorprocesses.DeviceProcessesMainView',
        'Mdc.view.setup.monitorprocesses.UsagePointProcessesMainView',
        'Mdc.view.setup.monitorprocesses.DeviceStartProcess',
        'Mdc.view.setup.monitorprocesses.UsagePointStartProcess'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'}
    ],

    showDeviceProcesses: function (deviceId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget;

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                widget = Ext.widget('device-processes-main-view', {
                    device: device,
                    properties: {
                        variableId: 'deviceId',
                        name: 'device',
                        value: device.get('mRID'),
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/task': null
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointProcesses: function (usagePointId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-main-view', {
                    router: router,
                    usagePointId: usagepoint.get('name'),
                    properties: {
                        variableId: 'usagePointId',
                        name: 'usagePoint',
                        value: usagepoint.get('mRID'),
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/task': null
                    }
            });
            me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(usagepoint.get('name'));

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showDeviceStartProcess: function (deviceId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget;

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                widget = Ext.widget('device-processes-start', {
                    device: device,
                    properties: {
                        activeProcessesParams: {
                            type: 'device',
                            deviceStates: device.data.state.id,
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'device'
                            },
                            {
                                name: 'id',
                                value: 'deviceId'
                            },
                            {
                                name: 'value',
                                value: device.get('mRID')
                            }
                        ],
                        additionalReasons: [Uni.I18n.translate('startProcess.empty.list.item', 'MDC', 'No processes are available for the current device state.')],
                        successLink: router.getRoute('devices/device/processes').buildUrl({deviceId: deviceId}),
                        cancelLink: router.getRoute('devices/device').buildUrl({deviceId: deviceId})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);


            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointStartProcess: function (usagePointId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-start', {
                    router: me.getController('Uni.controller.history.Router'),
                    usagePointId: usagepoint,
                    properties: {
                        activeProcessesParams: {
                            type: 'usagePoint',
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'usagePoint'
                            },
                            {
                                name: 'id',
                                value: 'usagePointId'
                            },
                            {
                                name: 'value',
                                value: usagepoint.get('mRID')
                            }
                        ],
                        successLink: router.getRoute('usagepoints/usagepoint/processes').buildUrl({usagePointId: usagePointId}),
                        cancelLink: router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: usagePointId})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(usagepoint.get('name'));
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    getPrivileges: function () {
        var executionPrivileges = [];

        Dbp.privileges.DeviceProcesses.canExecuteLevel1() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel1.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel2() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel2.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel3() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel3.toString()});
        Dbp.privileges.DeviceProcesses.canExecuteLevel4() && executionPrivileges.push({privilege: Dbp.privileges.DeviceProcesses.executeLevel4.toString()});

        return executionPrivileges;
    }
});
