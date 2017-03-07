/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.devicetype-security-accessors-preview-form',
    layout: 'fit',

    requires: [
        'Uni.form.field.ExecutionLevelDisplay'
    ],

    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                        name: 'description'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                        name: 'keyType',
                        renderer: function (value) {
                            return Ext.isEmpty(value) || Ext.isEmpty(value.name) ? '-' : value.name;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                        name: 'validityPeriod',
                        renderer: function (val) {
                            return Ext.isEmpty(val) ? '-' : val.count + ' ' + val.localizedTimeUnit;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('securityaccessors.viewPrivileges', 'MDC', 'View privileges'),
                        xtype: 'execution-level-displayfield',
                        name: 'viewLevelsInfo'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('securityaccessors.editPrivileges', 'MDC', 'Edit privileges'),
                        xtype: 'execution-level-displayfield',
                        name: 'editLevelsInfo'
                    }
                ]
            }
        ]
    }
});