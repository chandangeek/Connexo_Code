Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.validation-rule-preview',
    itemId: 'rulePreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleActionMenu'
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
                xtype: 'validation-rule-action-menu'
            }
        }
    ],

    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
    },

    items: [
        {
            name: 'displayName',
            fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                } else {
                    return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                }
            }
        },
        {
            xtype: 'container',
            itemId: 'readingTypesArea',
            items: []
        },
        {
            xtype: 'container',
            margin: '5 0 0 0',
            itemId: 'propertiesArea',
            items: []
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
