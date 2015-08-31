Ext.define('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    autoLoad: false,
    sorters: [{
        property: 'processId',
        direction: 'ASC'
    }],
    modelId: -1,
    removeAll: function(){
        this.callParent(arguments);
        this.modelId = -1;
    }
});