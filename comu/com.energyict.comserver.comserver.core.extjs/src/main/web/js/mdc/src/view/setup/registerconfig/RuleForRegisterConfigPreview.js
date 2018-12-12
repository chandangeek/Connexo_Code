/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registerconfig.RuleForRegisterConfigPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'validation-rule-for-register-config-preview',
    alias: 'widget.ruleForRegisterConfigPreview',
    itemId: 'ruleForRegisterConfigPreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule'
    ],

    title: Uni.I18n.translate('general.details','MDC','Details'),

    layout: {
        type: 'vbox'
    },

    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },

    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
        },
        {
            name: 'ruleSetName',
            fieldLabel: Uni.I18n.translate('validation.Rule', 'MDC', 'Rule')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateValidationRule: function (validationRule) {
        var me = this;

        me.loadRecord(validationRule);
        me.setTitle(validationRule.get('name'));
    }
});

