/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.validationConfigurationRulePreview',
    itemId: 'validationConfigurationRulePreview',
    frame: true,
    requires: [
        'Cfg.model.ValidationRule'
    ],
    title: '',
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
    },
    items: [
        {

            fieldLabel: Uni.I18n.translate('validation.validationRule', 'IMT', 'Validation rule'),
            name: 'name'
        },
        {
            fieldLabel: Uni.I18n.translate('validation.validator', 'IMT', 'Validator'),
            name: 'displayName'
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.active', 'IMT', 'Active')
                } else {
                    return Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                }
            }
        },
        {
            xtype: 'container',
            itemId: 'readingTypesArea',
            items: []
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
            var label = Uni.I18n.translate('validation.readingTypes', 'IMT', 'Reading types')
            readingType = selectedRule.data.readingTypes[i];

            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: label,
                    value: readingType
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});