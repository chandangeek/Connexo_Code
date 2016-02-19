Ext.define('Dbp.monitorprocesses.controller.MonitorProcesses', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    controllers: [
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    stores: [
    ],
    views: [
        'Dbp.monitorprocesses.view.DeviceProcessesMainView',
        'Dbp.monitorprocesses.view.UsagePointProcessesMainView'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'}
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.monitorprocesses.controller.MonitorProcesses'); // Forces registration.
    },

    showDeviceProcesses: function (mRID, model, sidePanel) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel(model).load(mRID, {
            success: function (device) {
                var widget;

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                widget = Ext.widget('dbp-device-processes-main-view', {
                    device: device,
                    properties: {
                        variableId: 'deviceId',
                        name: 'device',
                        value: mRID,
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/performTask': null
                    },
                    sidePanel: sidePanel
                });
                me.getApplication().fireEvent('changecontentevent', widget);

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    showUsagePointProcesses: function (usagePointId, model, sidePanel) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel(model).load(usagePointId, {
            success: function (usagepoint) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                viewport.setLoading(false);
                widget = Ext.widget('dbp-usage-point-processes-main-view', {
                    router: router,
                    mRID: usagepoint.get('mRID'),
                    properties: {
                        variableId: 'usagePointId',
                        name: 'usagePoint',
                        value: usagePointId,
                        route: Dbp.privileges.DeviceProcesses.canAssignOrExecute()? 'workspace/tasks/performTask': null
                    },
                    sidePanel: sidePanel
            });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(usagepoint.get('mRID'));

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    }
});
