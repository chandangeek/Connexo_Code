Ext.define('Dbp.deviceprocesses.controller.DeviceProcesses', {
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
        'Dbp.deviceprocesses.view.DeviceProcessesMainView'
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.monitorprocesses.controller.MonitorProcesses'); // Forces registration.
    },

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
                widget = Ext.widget('dbp-device-processes-main-view', {
                    device: device,
                    properties: {
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
    }
});
