Ext.define('Dbp.startprocess.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    controllers: [
        'Bpm.startprocess.controller.StartProcess'
    ],
    stores: [
        'Dbp.startprocess.store.AvailableProcesses'
    ],
    views: [
        'Dbp.startprocess.view.StartProcess'
    ],

    init: function () {
        var me = this;
        me.getController('Bpm.startprocess.controller.StartProcess'); // Forces registration.
    },

    showStartProcess: function (mRID) {
        var me = this,
            processesStore = Ext.getStore('Dbp.startprocess.store.AvailableProcesses'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget;

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);

                processesStore.getProxy().setUrl(device.data.state.id);
                processesStore.getProxy().extraParams = {privileges: Ext.encode(me.getPrivileges())};

                widget = Ext.widget('dbp-start-process-view', {
                    device: device,
                    properties: {
                        processesStore: processesStore,
                        extraParams: [{name: 'mrid', value: mRID}],
                        successLink: 'devices/device/processes',
                        cancelLink: 'devices/device'
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);

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