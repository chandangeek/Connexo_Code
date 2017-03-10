/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.CustomProperty', {
    extend: 'Ext.data.Model',
    fields: ['key', 'name', 'customPropertySet'],
    idProperty: 'key'
});