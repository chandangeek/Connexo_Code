Ext.define('Imt.usagepointlifecyclestates.model.TransitionBusinessProcess', {
    extend: 'Ext.data.Model',    
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'deploymentId', type: 'string'},
        {name: 'processId', type: 'string'}
    ]
});