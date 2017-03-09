/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.NumericalRegisterHistoryData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'rawValue', type: 'auto', useNull: true, defaultValue: null},
        {name: 'calculatedValue', type: 'string'},
        {name: 'calculatedUnit', type: 'string'},
        {name: 'multiplier', type: 'auto'}
    ]
});
