/**
 * @class Yfn.model.FilterInfo
 */
Ext.define('Yfn.model.FilterInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'filterType',
        'filterDisplayType',
        'filterName',
        'filterOmittable'
    ]
});