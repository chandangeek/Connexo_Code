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
        var properties = selectedRule.data.properties;
        this.down('#propertiesArea').removeAll();
        for (var i = 0; i < properties.length; i++) {
            var property = properties[i];
            var propertyName = property.name;
            var propertyValue = property.value;
            var required = property.required;
            var label = propertyName;
            if (!required) {
                label = label + ' (optional)';
            }
            this.down('#propertiesArea').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: label,
                    value: propertyValue,
                    labelWidth: 260
                }
            );
        }
    },

    addReadingTypes: function (selectedRule) {
        var readingTypes = selectedRule.data.readingTypes;
        this.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var readingType = readingTypes[i];
            var aliasName = readingType.aliasName;
            var mRID = readingType.mRID;
            var fieldlabel = Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading type(s)');
            if (i > 0) {
                fieldlabel = '&nbsp';
            }
            this.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 260,
                            width: 500,
                            value: mRID
                        },
                        {
                            xtype: 'component',
                            width: 500,
                            html: '<span style="color:grey"><i>' + aliasName + '</i></span>',
                            margin: '5 0 0 10'
                        }
                    ]
                }
            );
        }
    }
});
