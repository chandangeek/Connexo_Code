/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationPurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.util.CommonFields'
    ],
    alias: 'widget.metrology-config-purpose-detail-form',

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'purpose-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name')
            },
            {
                xtype: 'reading-type-displayfield',
                itemId: 'purpose-reading-type',
                fieldLabel: Uni.I18n.translate('general.readingType', 'IMT', 'Reading type')
            },
            {
                itemId: 'purpose-formula-description',
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula')
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'purpose-formula-components',
                fieldLabel: Uni.I18n.translate('general.formulaComponents', 'IMT', 'Formula components'),
                labelAlign: 'top',
                defaults: me.defaults
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            readingType = record.getReadingType(),
            formula = record.getFormula(),
            formulaComponentsContainer = me.down('#purpose-formula-components'),
            formulaComponents,
            customProperties;

        Ext.suspendLayouts();
        me.down('#purpose-reading-type').setValue(readingType.getData());
        me.down('#purpose-formula-description').setValue(formula.get('description'));

        formulaComponents = Imt.util.CommonFields.prepareReadingTypeRequirementFields(formula.readingTypeRequirements());
        customProperties = Imt.util.CommonFields.prepareCustomProperties(formula.customProperties());
        if (customProperties) {
            formulaComponents.push(customProperties);
        }

        formulaComponentsContainer.removeAll();
        formulaComponentsContainer.add(formulaComponents);
        Ext.resumeLayouts(true);

        me.callParent(arguments);
    }
});