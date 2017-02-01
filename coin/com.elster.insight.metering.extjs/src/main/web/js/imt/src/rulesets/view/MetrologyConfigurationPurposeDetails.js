/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.MetrologyConfigurationPurposeDetails', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-purpose-details',
    requires: [
        'Imt.rulesets.view.fields.Output'
    ],

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    items: [
        {
            itemId: 'metrology-configuration-field',
            name: 'metrologyConfigurationInfo',
            fieldLabel: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
            renderer: function (value) {
                return value.name;
            }
        },
        {
            itemId: 'metrology-configuration-status-field',
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.metrologyConfigurationStatus', 'IMT', 'Metrology configuration status'),
            renderer: function (value) {
                return value
                    ? Uni.I18n.translate('general.active', 'IMT', 'Active')
                    : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
            }
        },
        {
            itemId: 'purpose-field',
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose')
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'outputs-container',
            fieldLabel: Uni.I18n.translate('general.outputs', 'IMT', 'Outputs'),
            defaultType: 'output-display'
        }
    ],

    loadRecord: function (record) {
        var me = this,
            outputsContainer = me.down('#outputs-container'),
            outputs = record.get('outputs');

        Ext.suspendLayouts();
        me.callParent(arguments);
        outputsContainer.removeAll();
        if (!Ext.isEmpty(outputs)) {
            Ext.Array.each(outputs, addOutput);
        }
        Ext.resumeLayouts(true);

        function addOutput(output) {
            outputsContainer.add({
                value: output
            });
        }
    }
});