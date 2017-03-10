/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.CustomAttributeTypeDisplay
 */
Ext.define('Uni.form.field.CustomAttributeTypeDisplay', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.custom-attribute-type-displayfield',
    name: 'customAttributeType',
    fieldLabel: Uni.I18n.translate('general.type', 'UNI', 'Type'),
    emptyText: '',

    requires: [
        'Uni.view.window.CustomAttributeTypeDetails'
    ],

    handler: function (value) {
        var widget = Ext.widget('custom-attribute-type-details', {possibleValues: value.possibleValues});
        widget.setTitle('<span></span>');
        widget.show();
    },

    renderer: function (value, field, view, record) {
        var me = this,
            icon = '';

        if (!value) return this.emptyText;

        if (!Ext.isEmpty(value.possibleValues)) icon = '<span class="uni-icon-info-small" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: left;" data-qtip="' + Uni.I18n.translate('customattributetype.tooltip', 'UNI', 'Attribute type info') + '"></span>';

        setTimeout(function () {
            var parent,
                iconEl;

            if (Ext.isDefined(view) && Ext.isDefined(record)) {
                try {
                    parent = view.getCell(record, me);

                    if (Ext.isDefined(parent)) {
                        iconEl = parent.down('.uni-icon-info-small');
                    }
                } catch (ex) {
                    // Fails for some reason sometimes.
                }
            } else {
                parent = field.getEl();

                if (Ext.isDefined(parent)) {
                    iconEl = parent.down('.uni-icon-info-small');
                }
            }

            if (Ext.isDefined(iconEl) && !Ext.isEmpty(iconEl)) {
                iconEl.clearListeners();
                iconEl.on('click', function () {
                    field.handler(value);
                });
            }
        }, 1);

        return '<span style="display: inline-block; float: left; margin: 0px 10px 0px 0px">' + Ext.String.htmlEncode(value.name) + '</span>' + icon;
    }
});