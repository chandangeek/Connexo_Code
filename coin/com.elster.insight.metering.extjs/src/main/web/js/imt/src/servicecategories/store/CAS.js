/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.store.CAS', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesets.model.CustomAttributeSet',
    proxy: {
        type: 'rest',
        url: '/api/mtr/servicecategory/{serviceCategoryId}/custompropertysets',
        reader: {
            type: 'json',
            root: 'serviceCategoryCustomPropertySets'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});