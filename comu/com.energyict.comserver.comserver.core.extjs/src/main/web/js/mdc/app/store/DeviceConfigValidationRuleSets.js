Ext.define('Mdc.store.DeviceConfigValidationRuleSets', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.ValidationRuleSet'
    ],

    model: 'Mdc.model.ValidationRuleSet',
    storeId: 'DeviceConfigValidationRuleSets',

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/validationrulesets',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        }
    }
});