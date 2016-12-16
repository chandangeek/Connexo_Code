Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    requires: [
        //'Mdc.networkvisualiser.view.NetworkVisualiser'
    ],
    controllers: [

    ],
    stores: [
        'Uni.graphvisualiser.store.GraphStore'
    ],
    views: [
        'Mdc.networkvisualiser.view.NetworkVisualiserView'
    ],
    refs: [

    ],

    showVisualiser: function(){
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget;
        widget = Ext.widget('visualiserpanel');
        widget.store = Ext.getStore('Uni.graphvisualiser.store.GraphStore');
        me.getApplication().fireEvent('changecontentevent', widget);
    }
});
