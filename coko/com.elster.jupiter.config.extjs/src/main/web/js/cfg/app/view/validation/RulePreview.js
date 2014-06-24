Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.form.Panel',
    xtype: 'validation-rule-preview',
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
                xtype: 'rule-action-menu'
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
            fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name')
        },
        {
            name: 'displayName',
            fieldLabel: Uni.I18n.translate('validation.Rule', 'CFG', 'Rule')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('validation.active', 'CFG', 'Active'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.yes', 'CFG', 'Yes');
                } else {
                    return Uni.I18n.translate('general.no', 'CFG', 'No');
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
