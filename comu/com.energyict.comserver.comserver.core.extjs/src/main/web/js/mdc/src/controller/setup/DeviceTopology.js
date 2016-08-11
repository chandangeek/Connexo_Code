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

    showTopologyView: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceTopologyStore = me.getStore('Mdc.store.DeviceTopology'),
            widget;

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                var gatewayType = record.get('gatewayType');

                if (gatewayType === 'LAN' || gatewayType === 'HAN') {
                    widget = Ext.widget('deviceTopologySetup', {device: record, router: router});
                    me.getApplication().fireEvent('loadDevice', record);
                    me.getApplication().fireEvent('changecontentevent', widget);
                    deviceTopologyStore.getProxy().setUrl(record.get('mRID'));
                    deviceTopologyStore.load();
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
            }
        });
    }

});
