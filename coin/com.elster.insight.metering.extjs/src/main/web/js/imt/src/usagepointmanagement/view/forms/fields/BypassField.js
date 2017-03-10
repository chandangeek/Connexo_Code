/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.BypassField', {
    extend: 'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
    alias: 'widget.techinfo-bypassfield',
    name: 'bypass',
    fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass'),
    listeners: {
        afterrender: {
            fn: function (field) {
                field.nextSibling('techinfo-bypassstatuscombobox').setVisible(field.value === 'YES');
            }
        },
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    field.nextSibling('techinfo-bypassstatuscombobox').setVisible(newValue === 'YES');
                }
            }
        }
    }
});