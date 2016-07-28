Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.purposes-preview',
    router: null,
    title: ' ',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                itemId: 'purposes-preview-container',
                fieldLabel: Uni.I18n.translate('form.metrologyconfiguration.section.meterRoles', 'IMT', 'Meter roles'),
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                layout: 'vbox'
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'purpose-preview-formula-components',
                fieldLabel: Uni.I18n.translate('form.metrologyconfiguration.section.formulaComponents', 'IMT', 'Formula components'),
                layout: 'vbox',
                labelAlign: 'top',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                }
            }
        ];
        me.callParent(arguments);
    },

    clearFormulaComponents: function(){
        this.down('#purpose-preview-formula-components').removeAll();
    },

    addFormulaComponents: function (formula, upCustomPropertySets) {
        var me = this,
            formulaComponentsContainer = me.down('#purpose-preview-formula-components'),
            formulaComponents,
            customProperties;

        Ext.suspendLayouts();

        formulaComponents = Imt.util.CommonFields.prepareReadingTypeRequirementFields(formula.readingTypeRequirements());
        customProperties = Imt.util.CommonFields.prepareCustomProperties(formula.customProperties(), upCustomPropertySets);
        if (customProperties) {
            formulaComponents.push(customProperties);
        }

        formulaComponentsContainer.add(formulaComponents);
        Ext.resumeLayouts(true);
    }
});


