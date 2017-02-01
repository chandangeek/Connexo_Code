/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.EditUsagePointForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.edit-usage-point-form',
    requires: [
        'Uni.form.field.Duration'
    ],

    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'textfield',
                name: 'name',
                required: true,
                itemId: 'fld-up-name',
                width: 600,
                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
            },
            {
                xtype: 'combobox',
                name: 'serviceCategory',
                displayField: 'displayName',
                valueField: 'name',
                store: 'Mdc.usagepointmanagement.store.ServiceCategories',
                itemId: 'fld-up-serviceCategory',
                required: true,
                disabled: true,
                width: 600,
                fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'MDC', 'Service category')
            },
            {
                xtype: 'date-time',
                fieldLabel: Uni.I18n.translate('general.label.created', 'MDC', 'Created'),
                name: 'installationTime',
                itemId: 'installation-time-date',
                required: true,
                layout: 'hbox',
                valueInMilliseconds: true,
                dateConfig: {
                    width: 149
                },
                dateTimeSeparatorConfig: {
                    html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                    style: 'color: #686868'
                },
                hoursConfig: {
                    width: 75
                },
                minutesConfig: {
                    width: 75
                }
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: '&nbsp',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        xtype: 'button',
                        ui: 'action',
                        action: 'save',
                        itemId: 'usagePointSaveButton'
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'cancelLink',
                        href: me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: me.usagePointId})
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});