Ext.define('Imt.purpose.view.OutputSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.output-specifications-form',
    itemId: 'output-specifications-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.util.CommonFields'
    ],

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    padding: '10 0 0 0',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                name: 'name',
                itemId: 'output-name-field',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
            },
            {
                xtype: 'reading-type-displayfield',
                itemId: 'output-readingtype'
            },
            {
                xtype: 'displayfield',
                name: 'interval',
                itemId: 'output-interval-field',
                fieldLabel: Uni.I18n.translate('form.output.label.interval', 'IMT', 'Interval'),
                renderer: function (interval, field) {
                    if (Ext.isObject(interval)) {
                        field.show();
                        return interval.count + ' ' + interval.timeUnit;
                    } else {
                        field.hide();
                        return '';
                    }
                }
            },
            {
                xtype: 'displayfield',
                name: 'formula',
                itemId: 'output-formula-field',
                fieldLabel: Uni.I18n.translate('form.output.label.formula', 'IMT', 'Formula'),
                renderer: function (formula) {
                    return formula.description;
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'purpose-formula-components',
                fieldLabel: Uni.I18n.translate('form.output.section.formulaComponents', 'IMT', 'Formula components'),
                layout: 'vbox',
                labelAlign: 'top',
                defaults: me.defaults
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this,
            formula = record.getFormula(),
            formulaComponentsContainer = me.down('#purpose-formula-components'),
            formulaComponents,
            customProperties;

        Ext.suspendLayouts();
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