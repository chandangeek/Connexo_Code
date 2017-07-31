Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    models: [
        'Mdc.model.Device'
    ],
    stores: [
        'Uni.graphvisualiser.store.GraphStore',
        'Mdc.networkvisualiser.store.NetworkNodes'
    ],
    views: [
        'Mdc.networkvisualiser.view.NetworkVisualiserView'
    ],
    refs: [

    ],

    showVisualiser: function(){
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.create('Mdc.networkvisualiser.view.NetworkVisualiserView',{router: router});
        widget.clearGraph();
        widget.store = Ext.getStore('Uni.graphvisualiser.store.GraphStore');
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showNetwork: function(deviceId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.create('Mdc.networkvisualiser.view.NetworkVisualiserView', {router: router, yOffset: 45/*Due to the title*/}),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        widget.clearGraph();
        widget.store = Ext.getStore('Mdc.networkvisualiser.store.NetworkNodes');
        widget.store.getProxy().setUrl(deviceId);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    }
});
