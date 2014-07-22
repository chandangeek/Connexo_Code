Ext.define('Mdc.model.DeviceDataValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'status',
        'numberOfInactiveRules',
        'numberOfRules',
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
        url: '../../apps/mdc/app/store/DeviceRulesSetFake.json',
        reader: {
            type: 'json',
            root: 'validationRulesSet',
            totalProperty: 'total'
        }
    }
});

