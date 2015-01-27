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
            itemId: 'rulePreviewActionsButton',
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
            name: 'name',
            fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule')
        },
        {
            name: 'displayName',
            fieldLabel: Uni.I18n.translate('validation.validator', 'CFG', 'Validator')
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
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading types'),
            itemId: 'readingTypesArea'
        },
        {
            xtype: 'property-form',
            padding: '5 10 0 10',
            width: '100%',
            isEdit: false
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateValidationRule: function (validationRule) {
        var me = this;

        if (!Ext.isDefined(validationRule)) {
            return;
        }

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(validationRule);
        me.setTitle(validationRule.get('name'));
        me.addProperties(validationRule);
        me.addReadingTypes(validationRule);

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    addProperties: function (selectedRule) {
        this.down('property-form').loadRecord(selectedRule);
    },

    addReadingTypes: function (selectedRule) {
        Ext.suspendLayouts();
        this.down('#readingTypesArea').removeAll();
        for (var i = 0; i < selectedRule.data.readingTypes.length; i++) {
            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: selectedRule.data.readingTypes[i]
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});
