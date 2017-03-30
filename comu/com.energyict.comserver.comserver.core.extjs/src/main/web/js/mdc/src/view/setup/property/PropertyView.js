/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.property.PropertyView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.propertyView',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    itemId: 'propertyView',
    autoShow: true,
    border: 0,

    autoWidth: true,

    requires: ['Ext.form.Panel',
        'Mdc.widget.TimeInfoField'
    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                itemId: 'propertiesViewform',
                border: 0,
                layout: {
                    type: 'column',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 250,
                    anchor: '100%',
                    labelAlign: 'right'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.49,
                        layout: {
                            type: 'vbox'
                        },
                        itemId: 'propertyColumn1'
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.49,
                        layout: {
                            type: 'vbox'
                        },
                        itemId: 'propertyColumn0'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    addProperty: function (key, text, column) {
        var me = this;
        me.down('#propertyColumn' + column).add({
            xtype: 'displayfield',
            name: key,
            fieldLabel: Uni.I18n.translate('property.' + key, 'MDC', key),
            value: text,
            labelWidth: 250
        });
    }
});