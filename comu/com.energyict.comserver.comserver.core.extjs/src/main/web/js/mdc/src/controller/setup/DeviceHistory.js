Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicehistory.Setup',
        'Mdc.view.setup.devicehistory.LifeCycle'
    ],

    stores: [
        'Mdc.store.DeviceLifeCycleStatesHistory'
    ],

    models: [
        'Mdc.model.DeviceLifeCycleStatesHistory',
        'Mdc.model.Device'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-history-setup'
        }
    ],

    showDeviceHistory: function (mRID) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view;

        deviceModel.load(mRID, {
            success: function (device) {
                view = Ext.widget('device-history-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: device
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', view);
                me.showDeviceLifeCycleHistory();
            }
        });
    },

    showDeviceLifeCycleHistory: function () {
        var me = this,
            historyPanel = me.getPage().down('#history-panel'),
            lifeCyclePanel = Ext.widget('device-history-life-cycle-panel'),
            lifeCycleDataView = lifeCyclePanel.down('#life-cycle-data-view'),
            store = me.getStore('Mdc.store.DeviceLifeCycleStatesHistory');

        me.getPage().setLoading();
        historyPanel.add(lifeCyclePanel);
        lifeCycleDataView.bindStore(store);
        store.getProxy().setUrl(me.getController('Uni.controller.history.Router').arguments);
        store.load(function (records) {
            store.add(records.reverse());
            me.getPage().setLoading(false);
        });
    }
});
