/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.model.FilterInfo
 */
Ext.define('Yfn.model.FilterInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'filterType',
        'filterDisplayType',
        'filterName',
        'filterOmittable',
        'filterPrompt',
        'filterAllowPrompt',
        'filterDisplayName',
        'filterDefaultValue1',
        'filterDefaultValue2'
    ]
});