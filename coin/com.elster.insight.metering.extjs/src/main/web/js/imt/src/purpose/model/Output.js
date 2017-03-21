/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.Output', {
    extend: 'Uni.model.Version',
    requires: [
        'Imt.metrologyconfiguration.model.Formula',
        'Imt.usagepointmanagement.model.DataCompletion'
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
        {name: 'deliverableType', type: 'string', useNull: true},
        {name: 'hasEvent', type: 'auto', useNull: true},
        {name: 'isBilling', type: 'auto', useNull: true},
        {name: 'isCummulative', type: 'auto', useNull: true}
    ],

    associations: [
        {
            name: 'formula',
            type: 'hasOne',
            model: 'Imt.metrologyconfiguration.model.Formula',
            associationKey: 'formula',
            getterName: 'getFormula',
            setterName: 'setFormula'
        },
        {
            name: 'summary',
            type: 'hasOne',
            model: 'Imt.usagepointmanagement.model.DataCompletion',
            associationKey: 'summary',
            getterName: 'getSummary',
            setterName: 'setSummary'
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