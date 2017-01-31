/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.store.Attributes', {
    extend: 'Ext.data.Store',
    model: 'Cps.customattributesets.model.Attribute',
    requires: [
        'Cps.customattributesets.model.Attribute'
    ],
    autoLoad: false
});