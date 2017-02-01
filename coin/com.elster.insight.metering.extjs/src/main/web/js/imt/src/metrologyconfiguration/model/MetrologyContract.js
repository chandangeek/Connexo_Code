/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.MetrologyContract', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.metrologyconfiguration.model.ReadingTypeDeliverable'
    ],
    fields: ['id', 'name', 'mandatory', 'description',
        {
            name: 'active',
            persist: false,
            mapping: 'mandatory'
        }
    ],

    associations: [
        {
            name: 'readingTypeDeliverables',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.ReadingTypeDeliverable',
            associationKey: 'readingTypeDeliverables'
        }
    ]
});