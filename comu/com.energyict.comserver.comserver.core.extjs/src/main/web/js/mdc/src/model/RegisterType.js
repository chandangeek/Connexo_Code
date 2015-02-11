Ext.define('Mdc.model.RegisterType', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'isLinkedByDeviceType', type: 'boolean', useNull: true},
        {name: 'isLinkedByActiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'isLinkedByInactiveRegisterConfig', type: 'boolean', useNull: true},
        'readingType'
    ],
    idProperty: 'id',
    associations: [
        {
            name: 'readingType',
            associationKey: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registertypes'
    }
});