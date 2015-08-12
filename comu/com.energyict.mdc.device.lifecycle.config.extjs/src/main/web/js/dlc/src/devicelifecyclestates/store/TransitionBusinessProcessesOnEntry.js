Ext.define('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    autoLoad: false,
    modelId : -1,
    removeAll: function(){
        this.callParent(arguments);
        this.modelId = -1;
    }
});
