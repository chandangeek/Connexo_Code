Ext.define('Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecyclestates.view.Setup'
    ],

    stores: [
        'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates'
    ],

    models: [
        'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
        'Dlc.devicelifecycles.model.DeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycle-states-setup'
        }
    ],

    init: function () {
        this.control({
            'device-life-cycle-states-setup device-life-cycle-states-grid': {
                select: this.showDeviceLifeCycleStatePreview
            }
        });
    },

    showDeviceLifeCycleStates: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dlc.devicelifecyclestates.store.DeviceLifeCycleStates'),
            view;

        store.getProxy().setUrl(router.arguments);

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                view = Ext.widget('device-life-cycle-states-setup', {
                    router: router,
                    lifecycleRecord: deviceLifeCycleRecord
                });
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                view.down('#device-life-cycle-link').setText(deviceLifeCycleRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
                view.down('device-life-cycle-states-grid').getSelectionModel().select(0);
            }
        });

    },

    showDeviceLifeCycleStatePreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycle-states-preview'),
            previewForm = page.down('device-life-cycle-states-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});