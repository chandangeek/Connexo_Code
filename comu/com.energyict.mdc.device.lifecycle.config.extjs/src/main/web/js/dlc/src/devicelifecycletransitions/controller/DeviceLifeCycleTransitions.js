Ext.define('Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecycletransitions.view.Setup'
    ],

    stores: [
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions'
    ],

    models: [
        'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition',
        'Dlc.devicelifecycles.model.DeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycle-transitions-setup'
        }
    ],

    init: function () {
        this.control({
            'device-life-cycle-transitions-setup device-life-cycle-transitions-grid': {
                select: this.showDeviceLifeCycleTransitionPreview
            }
        });
    },

    showDeviceLifeCycleTransitions: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions'),
            view;

        store.getProxy().setUrl(router.arguments);

        view = Ext.widget('device-life-cycle-transitions-setup', {
            router: router
        });

        me.getApplication().fireEvent('changecontentevent', view);

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                view.down('#device-life-cycle-link').setText(deviceLifeCycleRecord.get('name'));
            }
        });
    },

    showDeviceLifeCycleTransitionPreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycle-transitions-preview'),
            previewForm = page.down('device-life-cycle-transitions-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});