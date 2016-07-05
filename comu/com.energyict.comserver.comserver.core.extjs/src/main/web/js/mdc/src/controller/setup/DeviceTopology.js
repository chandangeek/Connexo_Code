Ext.define('Mdc.controller.setup.DeviceTopology', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.DeviceTopology'
    ],

    views: [
        'Mdc.view.setup.devicetopology.Setup'
    ],

    refs: [
        {ref: 'deviceTopology', selector: '#deviceTopologySetup'},
        {ref: 'topFilter', selector: '#deviceTopologySetup #mdc-view-setup-devicechannels-topologiestopfilter'}
    ],

    init: function () {
        this.control({
            '#deviceTopologySetup #mdc-topology-edit-master-btn': {
                click: this.onEditMasterClicked
            },
            '#deviceTopologySetup #mdc-topology-remove-master-btn': {
                click: this.onRemoveMasterClicked
            },
            '#deviceTopologySetup #mdc-topology-edit-master-cancel-btn': {
                click: this.onEditMasterCancel
            },
            '#deviceTopologySetup #mdc-topology-edit-master-save-btn': {
                click: this.onEditMasterSave
            }
        });
    },

    showTopologyView: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceTopologyStore = me.getStore('Mdc.store.DeviceTopology'),
            widget;

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('deviceTopologySetup', { device: device, router: router });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceTopologyStore.getProxy().setUrl(device.get('mRID'));
                deviceTopologyStore.load();
            }
        });
    },

    onEditMasterClicked: function() {
        this.getDeviceTopology().addMasterContainerEditItems();
    },

    onEditMasterCancel: function() {
        this.getDeviceTopology().addMasterContainerViewItems();
    },

    onEditMasterSave: function() {
        this.getDeviceTopology().applyMasterDevice();
    },

    onRemoveMasterClicked: function() {
        this.getDeviceTopology().removeMasterDevice();
    }
});
