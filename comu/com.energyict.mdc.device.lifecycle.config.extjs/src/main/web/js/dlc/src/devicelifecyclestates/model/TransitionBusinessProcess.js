Ext.define('Dlc.devicelifecyclestates.model.TransitionBusinessProcess', {
    extend: 'Ext.data.Model',
    alias: 'transitionBusinessProcess',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'deploymentId', type: 'string'},
        {name: 'processId', type: 'string'}
    ]
});