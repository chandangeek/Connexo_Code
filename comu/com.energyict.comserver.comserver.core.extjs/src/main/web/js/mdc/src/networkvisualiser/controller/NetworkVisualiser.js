Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    requires: [
        //'Mdc.networkvisualiser.view.NetworkVisualiser'
    ],
    controllers: [

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
            widget = Ext.create('Mdc.networkvisualiser.view.NetworkVisualiserView', {router: router});
        widget.clearGraph();
        widget.store = Ext.getStore('Mdc.networkvisualiser.store.NetworkNodes');
        widget.store.getProxy().setUrl(deviceId);
       // widget.store.load(function() {
            me.getApplication().fireEvent('changecontentevent', widget);
       // });
    }
});
