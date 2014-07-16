Ext.define('Mdc.store.MeasurementTypesOnLoadProfileConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MeasurementType'
    ],
    autoload: false,
    model: 'Mdc.model.MeasurementType',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofileconfigurations/{loadProfileConfiguration}/measurementTypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});