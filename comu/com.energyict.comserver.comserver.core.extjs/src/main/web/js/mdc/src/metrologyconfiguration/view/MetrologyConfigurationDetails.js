/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.metrologyconfiguration.view.MetrologyConfigurationDetails', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Mdc.metrologyconfiguration.view.ActionsMenu'
    ],
    alias: 'widget.metrology-configuration-details',

    layout: 'column',

    defaults: {
        columnWidth: 0.5
    },

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'metrology-configuration-actions-button',
                privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                menu: {
                    xtype: 'metrology-configuration-actions-menu',
                    itemId: 'metrology-configuration-actions-menu'
                }
            }
        ];

        me.items = [
            {
                xtype: 'container',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'metrology-configuration-name',
                        name: 'name',
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
                    },
                    {
                        itemId: 'metrology-configuration-description',
                        name: 'description',
                        fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description')
                    },
                    {
                        itemId: 'metrology-configuration-status',
                        name: 'status',
                        fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                        renderer: function (value) {
                            return value ? value.name : '';
                        }
                    },
                    {
                        itemId: 'metrology-configuration-service-category',
                        name: 'serviceCategory',
                        fieldLabel: Uni.I18n.translate('general.serviceCategory', 'MDC', 'Service category'),
                        renderer: function (value) {
                            return value ? value.name : '';
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'metrology-configuration-reading-types',
                fieldLabel: Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                labelWidth: 200,
                defaults: {
                    xtype: 'reading-type-displayfield',
                    labelWidth: 0,
                    fieldLabel: undefined
                }
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            readingTypesContainer = me.down('#metrology-configuration-reading-types'),
            newFields = [];

        Ext.suspendLayouts();
        readingTypesContainer.removeAll();
        Ext.Array.each(record.get('readingTypes'), function (readingType) {
            if (Ext.isObject(readingType)) {
                newFields.push({
                    value: readingType
                });
            }
        });
        if (newFields.length) {
            readingTypesContainer.add(newFields);
        }
        Ext.resumeLayouts(true);

        me.callParent(arguments);
    }
});