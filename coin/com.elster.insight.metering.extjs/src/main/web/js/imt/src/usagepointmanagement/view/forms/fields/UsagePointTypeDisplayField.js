/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.UsagePointTypeDisplayField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.usagepointtypedisplayfield',

    renderer: function (value) {
        var result = '-',
            type;

        if (!Ext.isEmpty(value)) {
            result = Ext.getStore('Imt.usagepointmanagement.store.AllUsagePointTypes').getById(value).get('displayName');
        }

        return result;
    }
});