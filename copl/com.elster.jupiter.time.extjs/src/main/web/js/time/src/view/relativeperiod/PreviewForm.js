/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.relative-periods-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name'),
                name: 'name'
            },
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                name: 'listOfCategories'
            },
            {
                xtype: 'panel',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        labelWidth: 250,
                        fieldLabel: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview'),
                        emptyValueDisplay: ''
                    },
                    {
                        xtype: 'uni-form-relativeperiodpreview-basedOnId'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});