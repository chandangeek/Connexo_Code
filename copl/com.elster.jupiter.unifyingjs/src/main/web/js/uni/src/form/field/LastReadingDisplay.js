/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.LastReadingDisplay
 */
Ext.define('Uni.form.field.LastReadingDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'last-reading-displayfield',
    name: 'lastReading',
    fieldLabel: Uni.I18n.translate('lastReading.label', 'UNI', 'Last reading'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'uni-icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (value, field) {
        var result = Uni.DateTime.formatDateTimeLong(Ext.isDate(value) ? value : new Date(value)),
            tooltip = Uni.I18n.translate('lastReading.tooltip', 'UNI', 'The moment when the data was read out for the last time');

        if (!value) {
            return this.emptyText;
        }

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; float: left; margin-right: 10px;">' + result + '</span>';
    }
});