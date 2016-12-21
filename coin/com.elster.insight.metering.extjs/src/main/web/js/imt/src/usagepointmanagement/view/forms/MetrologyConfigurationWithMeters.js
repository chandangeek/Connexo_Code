Ext.define('Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMeters', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-with-meters-info-form',
    requires: [
        'Uni.form.field.ComboReturnedRecordData',
        'Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMetersInfo',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration'
    ],

    usagePoint: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'component',
                itemId: 'link-metrology-configuration-with-meters-step-description',
                html: Uni.I18n.translate('usagepoint.wizard.linkMetrologyConfigurationWithMetersStep.description', 'IMT', 'Link a metrology configuration, and link meters to meter roles of the selected metrology configuration.'),
                style: 'margin: -3px 0 13px 0; font-style: italic'
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'metrology-configuration-container',
                labelWidth: 260,
                fieldLabel: Uni.I18n.translate('general.label.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                layout: 'hbox'
            }
        ];

        me.callParent(arguments);
    },

    prepareStep: function (hasAvailableMetrologyConfigurations) {
        var me = this,
            metrologyConfigurationContainer = me.down('#metrology-configuration-container'),
            metrologyConfigurationInfo = me.down('#metrology-configuration-with-meters-info');

        Ext.suspendLayouts();
        metrologyConfigurationContainer.removeAll();
        if (metrologyConfigurationInfo) {
            metrologyConfigurationInfo.destroy();
        }
        if (hasAvailableMetrologyConfigurations) {
            metrologyConfigurationContainer.add([
                {
                    xtype: 'combo-returned-record-data',
                    name: 'id',
                    itemId: 'metrology-configuration-combo',
                    afterSubTpl: '<span class="field-additional-info" style="color: #686868; font-style: italic">'
                    + Uni.I18n.translate('metrologyConfiguration.wizard.clarification', 'IMT', 'The metrology configurations applicable to the usage point.')
                    + '</span>',
                    store: 'Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations',
                    displayField: 'name',
                    valueField: 'id',
                    queryMode: 'local',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('metrologyConfiguration.wizard.emptyText', 'IMT', 'Select metrology configuration...'),
                    width: 320,
                    listeners: {
                        change: Ext.bind(me.onMetrologyConfigurationChange, me)
                    }
                },
                {
                    xtype: 'button',
                    itemId: 'reset-metrology-configuration',
                    iconCls: 'icon-rotate-ccw3',
                    tooltip: Uni.I18n.translate('general.reset', 'IMT', 'Reset'),
                    disabled: true,
                    width: 30,
                    margin: '0 0 0 20',
                    handler: Ext.bind(me.resetMetrologyConfiguration, me)
                }
            ]);
            me.add({
                xtype: 'metrology-configuration-with-meters-info',
                itemId: 'metrology-configuration-with-meters-info'
            });
        } else {
            metrologyConfigurationContainer.add({
                xtype: 'displayfield',
                itemId: 'no-available-metrology-configurations-message',
                htmlEncode: false,
                value: '<span style="color: #686868; font-style: italic">'
                + Uni.I18n.translate('metrologyConfiguration.wizard.noAvailable', 'IMT', 'No available metrology configurations')
                + '</span>'
            });
        }
        Ext.resumeLayouts(true);
    },

    onMetrologyConfigurationChange: function (combo, newValue) {
        var me = this,
            metrologyConfigurationInfo = me.down('#metrology-configuration-with-meters-info'),
            meterActivationsField = me.down('#meter-activations-field'),
            purposesField = me.down('#purposes-field');

        Ext.suspendLayouts();
        me.down('#reset-metrology-configuration').setDisabled(!newValue);
        if (!Ext.isEmpty(newValue)) {
            meterActivationsField.show();
            purposesField.show();
            metrologyConfigurationInfo.setLoading();
            Ext.ModelManager.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration').load(newValue.id, {
                success: function (record) {
                    Ext.suspendLayouts();
                    meterActivationsField.setMeterRoles(record.get('meterRoles'), me.usagePoint.get('installationTime'));
                    purposesField.setStore(record.metrologyContracts());
                    Ext.resumeLayouts(true);
                },
                callback: function () {
                    metrologyConfigurationInfo.setLoading(false);
                }
            });
        } else {
            meterActivationsField.hide();
            purposesField.hide();
        }
        Ext.resumeLayouts(true);
    },

    resetMetrologyConfiguration: function () {
        var me = this;

        me.down('#metrology-configuration-combo').reset();
    },

    getRecord: function () {
        var me = this,
            metrologyConfigurationCombo = me.down('#metrology-configuration-combo'),
            meterActivationsField = me.down('#meter-activations-field'),
            purposesField = me.down('#purposes-field'),
            metrologyConfiguration = metrologyConfigurationCombo ? metrologyConfigurationCombo.getValue() : null,
            meterActivations = null;

        if (metrologyConfiguration) {
            metrologyConfiguration.purposes = purposesField.getValue();
            meterActivations = meterActivationsField.getValue();
        }

        return {
            metrologyConfiguration: metrologyConfiguration,
            meterActivations: meterActivations
        }
    }
});