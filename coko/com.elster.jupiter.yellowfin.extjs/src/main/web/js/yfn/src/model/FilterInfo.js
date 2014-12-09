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
        'filterOmittable',
        'filterPrompt',
        'filterAllowPrompt',
        'filterDisplayName',
        'filterDefaultValue1',
        'filterDefaultValue2'
    ]
});