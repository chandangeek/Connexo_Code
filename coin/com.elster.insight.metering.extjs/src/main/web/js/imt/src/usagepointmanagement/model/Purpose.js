Ext.define('Imt.usagepointmanagement.model.Purpose', {
    extend: 'Ext.data.Model',
    requires: ['Imt.usagepointmanagement.model.ValidationInfo'],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'status', type: 'auto', useNull: true}
    ],
    associations: [{
        type: 'hasOne',
        model: 'Imt.usagepointmanagement.model.ValidationInfo',
        name: 'validationInfo',
        foreignKey: 'validationInfo',
        associationKey: 'validationInfo',
        getterName: 'getValidationInfo'
    }]
});