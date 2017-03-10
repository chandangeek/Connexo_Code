/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.CustomAttributeSetDisplay
 */
Ext.define('Uni.form.field.CustomAttributeSetDisplay', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.custom-attribute-set-displayfield',
    name: 'fullsetinfo',
    fieldLabel: Uni.I18n.translate('general.name', 'UNI', 'Name'),
    emptyText: '',

    requires: [
        'Uni.view.window.CustomAttributeSetDetails'
    ],

    handler: function (value) {
        var widget = Ext.widget('custom-attribute-set-details', {record: value});
        widget.setTitle('<span></span>');
        widget.show();
    },

    renderer: function (value, field, view, record) {
        var me = this,
            icon = '<span class="uni-icon-info-small" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: left;" data-qtip="' + Uni.I18n.translate('customattributeset.tooltip', 'UNI', 'Attribute set details') + '"></span>';

        if (!value) return this.emptyText;

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

        return '<span style="display: inline-block; float: left; margin: 0px 8px 0px 0px">' + Ext.String.htmlEncode(value.name) + '</span>' + icon;
    }
});