Ext.define('Mdc.model.DeviceDataValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isActive', type: 'boolean'},
        {name: 'numberOfInactiveRules', type: 'int'},
        {name: 'numberOfRules', type: 'int'},
        {
            name: 'numberOfActiveRules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/validationrulesets',
        reader: {
            type: 'json',
            root: 'rulesets',
            totalProperty: 'total'
        }
    }
});