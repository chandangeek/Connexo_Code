Ext.define('Imt.model.SearchCriteria', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'criteriaName', type: 'string'},
        {name: 'criteriaValues', type: 'auto'}
    ]
});
