Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicehistory.Setup',
        'Mdc.view.setup.devicehistory.LifeCycle'
    ],

    stores: [

    ],

    models: [

    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-history-setup'
        },
        {
            ref: 'lifeCyclePanel',
            selector: 'device-history-life-cycle-panel'
        }
    ],

    init: function () {
        this.control({
            'device-history-setup #device-history-tab-panel': {
                tabchange: this.onTabChange
            }
        });
    },

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
                view.down('#device-history-tab-panel').setActiveTab(1);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    onTabChange: function (tabPanel, newCard, oldCard) {
        var me = this,
            page = me.getPage(),
            historyPanel = page.down('#history-panel'),
            existedAdditionalItem = historyPanel.down('#device-history-tab-panel').next(),
            additionalItem;

        existedAdditionalItem && historyPanel.remove(existedAdditionalItem);

        switch (newCard.itemId) {
            case 'device-history-life-cycle-tab':
                additionalItem = Ext.widget('device-history-life-cycle-panel');
                me.showDeviceLifeCycleHistory();
                break;
        }

        historyPanel.add(additionalItem);
    },

    showDeviceLifeCycleHistory: function () {
        var me = this,
            lifeCycleDataView = me.getLifeCyclePanel().down('#life-cycle-data-view'),
            store = me.getStore('');

        lifeCycleDataView.bindStore(store);
        lifeCycleDataView.setLoading();
        store.load(function (records) {
            store.add(records);
            lifeCycleDataView.setLoading(false);
        });
    }
});
