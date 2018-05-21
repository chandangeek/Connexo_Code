/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.validation-rule-preview',
    itemId: 'rulePreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleActionMenu'
    ],

    title: Uni.I18n.translate('general.details', 'IMT', 'Details'),

    layout: {
        type: 'vbox'
    },

    noActionsButton: false,

    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
    },

    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('validation.validationRule', 'IMT', 'Validation rule')
        },
        {
            name: 'displayName',
            fieldLabel: Uni.I18n.translate('validation.validator', 'IMT', 'Validator')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('validation.active', 'IMT', 'Active')
                } else {
                    return Uni.I18n.translate('validation.inactive', 'IMT', 'Inactive')
                }
            }
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('validation.readingTypes', 'IMT', 'Reading types'),
            itemId: 'readingTypesArea',
            margin: '0 0 0 0'
        },
        {
            name: 'action',
            margin: '0 0 -8 0',
            fieldLabel: Uni.I18n.translate('validation.dataQualityLevel', 'IMT', 'Data quality level'),
            renderer: function (value) {
                if (value == "FAIL") {
                    return Uni.I18n.translate('validation.dataQualityLevelFail', 'IMT', 'Suspect')
                } if (value == "WARN_ONLY") {
                    return Uni.I18n.translate('validation.dataQualityLevelWarnOnly', 'IMT', 'Informative')
                }else {
                    return Uni.I18n.translate('general.none', 'IMT', 'None')
                }
            }
        },
        {
            xtype: 'property-form',
            padding: '10 10 0 10',
            width: '100%',
            isEdit: false
        }
    ],

    initComponent: function () {
        var me = this;        
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'rulePreviewActionsButton',
                privileges: Cfg.privileges.Validation.admin,
                hidden: me.noActionsButton,
                menu: {
                    xtype: 'validation-rule-action-menu'
                }
            }
        ];
        me.callParent(arguments);
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
        me.setTitle(Ext.String.htmlEncode(validationRule.get('name')));
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
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('validation.readingTypes', 'IMT', 'Reading types'),
                readingType = selectedRule.data.readingTypes[i];

            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});
