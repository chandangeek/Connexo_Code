/**
 * @class Uni.model.search.Result
 */
Ext.define('Uni.model.search.Value', {
    extend: 'Ext.data.Model',
    idProperty: null,
    fields: [
        {name: 'operator', type: 'string'},
        {name: 'criteria', type: 'auto'},
        {name: 'filter', type: 'string'}
    ]
});