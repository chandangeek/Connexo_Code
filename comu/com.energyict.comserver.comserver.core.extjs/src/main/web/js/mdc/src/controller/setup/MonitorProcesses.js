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

    showDeviceProcesses: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget;

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                widget = Ext.widget('device-processes-main-view', {
                    device: device,
                    properties: {
                        variableId: 'deviceId',
                        name: 'device',
                        value: mRID,
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/performTask': null
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
                    mRID: usagepoint.get('mRID'),
                    properties: {
                        variableId: 'usagePointId',
                        name: 'usagePoint',
                        value: usagePointId,
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/performTask': null
                    }
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.getOverviewLink().setText(usagepoint.get('mRID'));

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showDeviceStartProcess: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
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
                                value: mRID
                            }
                        ],
                        additionalReasons: [Uni.I18n.translate('startProcess.empty.list.item', 'MDC', 'No processes are available for the current device state.')],
                        successLink: router.getRoute('devices/device/processes').buildUrl({mRID: mRID}),
                        cancelLink: router.getRoute('devices/device').buildUrl({mRID: mRID})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);


            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointStartProcess: function (id) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(id, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-start', {
                    router: me.getController('Uni.controller.history.Router'),
                    mRID: usagepoint,
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
                                value: id
                            }
                        ],
                        successLink: router.getRoute('usagepoints/usagepoint/processes').buildUrl({usagePointId: id}),
                        cancelLink: router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: id})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(usagepoint.get('mRID'));
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
