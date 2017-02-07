/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.Output', {
    extend: 'Uni.model.Version',
    requires: [
        'Imt.metrologyconfiguration.model.Formula'
    ],

    fields: [
        'validationInfo',
        'outputType',
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'readingType', type: 'auto', useNull: true},
        {name: 'formula', type: 'auto', useNull: true},
        {name: 'flowUnit', type: 'string', useNull: true},
        {name: 'deliverableType', type: 'string', useNull: true}
    ],

    associations: [
        {
            name: 'formula',
            type: 'hasOne',
            model: 'Imt.metrologyconfiguration.model.Formula',
            associationKey: 'formula',
            getterName: 'getFormula',
            setterName: 'setFormula'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs',
        reader: 'json',
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});