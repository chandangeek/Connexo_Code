/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.model.ReportInfo
 */
Ext.define('Yfn.model.ReportInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'reportUUID',
        'reportId',
        'name',
        'description',
        'category',
        'subCategory'
    ]
});