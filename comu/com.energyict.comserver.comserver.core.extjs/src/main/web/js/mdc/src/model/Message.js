Ext.define('Mdc.model.Message', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'active', type: 'boolean'},
        {name: 'privileges', type: 'auto'}
    ]
});