Ext.define('Mdc.controller.setup.DeviceTopology', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.DeviceTopology'
    ],

    views: [
        'Mdc.view.setup.devicetopology.Setup'
    ],

    refs: [],

    init: function () {

    },

    showTopologyView: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            deviceTopologyStore,
            widget;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('deviceTopologySetup', { device: device, router: router });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceTopologyStore = widget.down('#deviceTopologyGrid').getStore();
                deviceTopologyStore.getProxy().setUrl(device.get('mRID'));
                deviceTopologyStore.load();
                viewport.setLoading(false);
            }
        });
    }

});
