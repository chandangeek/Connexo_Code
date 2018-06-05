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
    //width:'100%',
    // layout: {
    //     type: 'vbox'
    //     align: 'stretch'
    //     autoSize:true,
    // },
    layout: {
        type: 'vbox',
        // align: 'stretch'
    },
    // style: {
    //     width: '100%'
    // },
    // flex:1,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
        //width: '100%'
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
            width: '100%',
            isEdit: false,
            layout: {
                type: 'vbox'
                // align: 'stretch'
            },
            flex: 1,
            defaults: {
                labelWidth: 260,
                width: 500
            }
        }
    ],

    updateValidationRule: function (validationRule) {
        //return;
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
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('validation.readingTypes', 'IMT', 'Reading types')
            readingType = selectedRule.data.readingTypes[i];

            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: fieldlabel,
                    value: readingType,
                    labelWidth: 260
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});