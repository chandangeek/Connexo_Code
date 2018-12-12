/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('PageAnalyzer.models.LayoutIdSummaryData', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'type', type: 'string' },
        { name: 'duration', type: 'float', defaultValue: 0.0 },
        { name: 'count', type: 'int', defaultValue: 0 }
    ]

});
