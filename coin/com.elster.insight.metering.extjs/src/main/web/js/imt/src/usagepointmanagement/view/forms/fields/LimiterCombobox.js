/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.LimiterCombobox', {
    extend: 'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
    alias: 'widget.techinfo-limiter-combo',
    name: 'limiter',
    fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
    listeners: {
        afterrender: {
            fn: function (field) {
                field.showChildField(field.value === 'YES');
            }
        },
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    field.showChildField(newValue === 'YES');
                }
            }
        }
    },
    showChildField: function (value) {
        Ext.suspendLayouts();
        this.nextSibling('techinfo-loadlimitertypefield').setVisible(value);
        this.nextSibling('techinfo-loadlimitfield').setVisible(value);
        Ext.resumeLayouts(true);
    }
});