Ext.define('Imt.usagepointmanagement.model.Purpose', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'status', type: 'auto', useNull: true}
    ]
});