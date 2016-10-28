Ext.define('Mdc.controller.setup.DeviceCommunicationPlanning', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanning'
    ],

    stores: [
        'DeviceSchedules'
    ],

    requires: [
        'Mdc.store.DeviceSchedules'
    ],

    deviceMRID: undefined,

    showDeviceCommunicationPlanning: function(deviceMRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceMRID = deviceMRID;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceMRID, {
            success: function (device) {
                var scheduleStore = me.getDeviceSchedulesStore();
                scheduleStore.getProxy().setUrl(deviceMRID);
                scheduleStore.load({
                    callback: function () {
                        var widget = Ext.widget('deviceCommunicationPlanning', {
                            device: device,
                            scheduleStore: scheduleStore
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);

                        if (scheduleStore.getCount() === 0) {
                            widget.down('#mdc-device-communication-planning-grid-msg').show();
                            widget.down('#mdc-device-communication-planning-grid grid').hide();
                        } else {
                            widget.down('#mdc-device-communication-planning-grid-msg').hide();
                            widget.down('#mdc-device-communication-planning-grid grid').show();
                        }
                    }
                })
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    }
});