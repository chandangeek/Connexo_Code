Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    requires: [
        //'Mdc.networkvisualiser.view.NetworkVisualiser'
    ],
    controllers: [

    ],
    stores: [
    ],
    views: [
        'Mdc.networkvisualiser.view.NetworkVisualiserView'
    ],
    refs: [

    ],

    showVisualiser: function(){
        debugger;
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget;
        widget = Ext.widget('visualiserpanel');
        me.getApplication().fireEvent('changecontentevent', widget);
    }
});
