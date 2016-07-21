Ext.define('Imt.usagepointmanagement.model.Purpose', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'lastChecked', type: 'auto', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes',
        reader: {
            type: 'json'            
        }        
    }
});