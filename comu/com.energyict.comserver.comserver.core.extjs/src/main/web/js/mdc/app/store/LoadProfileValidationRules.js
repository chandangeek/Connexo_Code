Ext.define('Mdc.store.LoadProfileValidationRules', {
    extend: 'Ext.data.Store',

    requires: [
        'Cfg.model.ValidationRule'
    ],

    model: 'Cfg.model.ValidationRule',
    autoLoad: false,
    storeId: 'LoadProfileValidationRules',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofiles/{loadProfileConfig}/validationrules',

        //url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registers/{registerConfig}/validationrules',
        reader: {
            type: 'json',
            root: 'validationRules',
            totalProperty: 'total'
        }
    }
});
