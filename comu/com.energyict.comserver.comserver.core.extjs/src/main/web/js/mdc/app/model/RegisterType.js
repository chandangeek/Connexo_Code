Ext.define('Mdc.model.RegisterType', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
       // {name: 'phenomenon', type: 'auto'},
        {name: 'isLinkedByDeviceType', type: 'boolean', useNull: true},
        {name: 'isLinkedByActiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'isLinkedByInactiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'timeOfUse', type: 'number', useNull: true},
        {name: 'unit', type: 'string', useNull: true},
        'readingType'
    ],
    idProperty: 'id',
    associations: [
        {name: 'readingType', type: 'hasOne', model: 'Mdc.model.ReadingType', associationKey: 'readingType',
            getterName: 'getReadingType', setterName: 'setReadingType', foreignKey: 'readingType'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registertypes'
    }
});