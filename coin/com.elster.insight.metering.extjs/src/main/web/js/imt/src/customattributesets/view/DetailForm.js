/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesets.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cas-detail-form',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.attributes', 'IMT', 'Attributes'),
                name: 'properties',
                renderer: function (value) {
                    var result = '',
                        requiredIcon = '<span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                            + Uni.I18n.translate('general.required', 'IMT', 'Required')
                            + '"></span>';

                    if (Ext.isArray(value)) {
                        Ext.Array.each(value, function (attribute, index) {
                            result += (index
                                    ? '<br/><br/>'
                                    : '')
                                + Ext.String.htmlEncode(attribute.name)
                                + (attribute.required
                                    ? requiredIcon
                                    : '');
                        });
                    }

                    return result;
                }
            }
        ];

        me.callParent(arguments);
    }
});