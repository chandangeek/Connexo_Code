/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.PreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'devicegroups-preview-form',

    border: false,
    itemId: 'deviceGroupPreviewForm',
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
                            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                            itemId: 'deviceGroupName'

                },
                {
                            xtype: 'displayfield',
                            name: 'dynamic',
                            fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.I18n.translate('general.dynamic', 'MDC', 'Dynamic')
                                } else {
                                    return Uni.I18n.translate('general.static', 'MDC', 'Static')
                                }
                            }
                }

      ]

});
