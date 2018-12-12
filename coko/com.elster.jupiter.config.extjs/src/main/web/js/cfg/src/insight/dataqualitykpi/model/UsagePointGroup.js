/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.model.UsagePointGroup', {
    extend:'Ext.data.Model',
    requires: [
        'Cfg.insight.dataqualitykpi.model.Purpose'
    ],

    fields: [
        'id',
        'name'
    ],

    associations: [
        {
            name: 'purposes',
            type: 'hasMany',
            model: 'Cfg.insight.dataqualitykpi.model.Purpose',
            associationKey: 'purposes'
        }
    ]
});
