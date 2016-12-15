Ext.define('Imt.usagepointmanagement.model.Purpose', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'validationInfo', defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes',
        reader: {
            type: 'json'
        }
    }
});