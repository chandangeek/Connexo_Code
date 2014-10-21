Ext.define('Mdc.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.SearchCriteria'
    ],
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'dynamic', type: 'boolean', useNull: true},
        {name: 'filter', type: 'auto', useNull: true}
    ],

    associations: [
        {
            type: 'hasMany',
            model: 'Mdc.model.SearchCriteria',
            associationKey: 'criteria',
            name: 'criteria'
        }
    ],

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    }

});