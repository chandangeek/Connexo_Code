Ext.define('Dlc.devicelifecyclestates.store.TransitionBusinessProcesses', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    storeId: 'TransitionBusinessProcessesForState',
    autoLoad: false,
});