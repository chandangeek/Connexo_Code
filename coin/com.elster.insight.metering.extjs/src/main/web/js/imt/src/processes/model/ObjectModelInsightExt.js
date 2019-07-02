Ext.define('Imt.processes.model.ObjectModelInsightExt', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'mrId', type: 'string'}
    ],
    idProperty: 'name',
});