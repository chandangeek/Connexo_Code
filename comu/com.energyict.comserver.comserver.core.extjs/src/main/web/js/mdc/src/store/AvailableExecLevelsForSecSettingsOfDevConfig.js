Ext.define('Mdc.store.AvailableExecLevelsForSecSettingsOfDevConfig', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ExecutionLevel'
    ],
    model: 'Mdc.model.ExecutionLevel',
    storeId: 'AvailableExecLevelsForSecSettingsOfDevConfig',
    proxy: {
        type: 'rest',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties/{securityProperty}/executionlevels',
        reader: {
            type: 'json',
            root: 'executionLevels'
        }
    }
});
