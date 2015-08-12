Ext.define('Mdc.view.setup.registerconfig.RuleForRegisterConfigPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'validation-rule-for-register-config-preview',
    alias: 'widget.ruleForRegisterConfigPreview',
    itemId: 'ruleForRegisterConfigPreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule',
        'Mdc.view.setup.registerconfig.RulesForRegisterConfigActionMenu'
    ],

    title: 'Details',

    layout: {
        type: 'vbox'
    },

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'rules-for-registerconfig-actionmenu'
            }
        }
    ],

    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },

    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('validation.name', 'MDC', 'Name')
        },
        {
            name: 'ruleSetName',
            fieldLabel: Uni.I18n.translate('validation.Rule', 'MDC', 'Rule')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('validation.status', 'MDC', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('validation.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('validation.inactive', 'MDC', 'Inactive');
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

