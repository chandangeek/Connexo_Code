/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepointgroup-preview-form',
    xtype: 'usagepointgroup-preview-form',
    border: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150
    },

    items: [
        {
            xtype: 'displayfield',
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
            itemId: 'usageppointgroup-name'
        },
        {
            xtype: 'displayfield',
            name: 'dynamic',
            itemId: 'usageppointgroup-type',
            fieldLabel: Uni.I18n.translate('general.type', 'IMT', 'Type'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.dynamic', 'IMT', 'Dynamic')
                } else {
                    return Uni.I18n.translate('general.static', 'IMT', 'Static')
                }
            }
        }
    ]
});
