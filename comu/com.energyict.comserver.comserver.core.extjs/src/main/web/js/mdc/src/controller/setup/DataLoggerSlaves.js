Ext.define('Mdc.controller.setup.DataLoggerSlaves', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.DataLoggerSlaves'
    ],

    views: [
        'Mdc.view.setup.dataloggerslaves.Setup'
    ],

    init: function () {
        this.control({
            '#mdc-link-dataloggerslave-btn': {
                click: this.onLinkDataLoggerSlave
            }
        });
    },

    showDataLoggerSlaves: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            slavesStore = me.getStore('Mdc.store.DataLoggerSlaves'),
            widget;

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                slavesStore.getProxy().setUrl(device.get('mRID'));
                widget = Ext.widget('dataLoggerSlavesSetup', { device: device, router: router, store:slavesStore });
                me.getApplication().fireEvent('changecontentevent', widget);
                slavesStore.load();
            }
        });
    },

    onLinkDataLoggerSlave: function() {
        console.log('To do: startup link slave wizard');
    }

});

