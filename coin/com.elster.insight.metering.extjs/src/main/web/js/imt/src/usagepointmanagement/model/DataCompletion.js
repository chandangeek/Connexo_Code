Ext.define('Imt.usagepointmanagement.model.DataCompletion', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'statistics', type: 'auto', useNull: true}
    ]
});