/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        //url: 'http://localhost:3000/measurementTypes',
        pageParam: false,
        limitParam: false,
        startParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});