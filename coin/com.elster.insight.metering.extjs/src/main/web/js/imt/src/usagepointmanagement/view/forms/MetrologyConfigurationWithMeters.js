Ext.define('Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMeters', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-with-meters-info-form',
    requires: [],

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
            metrologyConfigurationContainer = me.down('#metrology-configuration-container');

        Ext.suspendLayouts();
        metrologyConfigurationContainer.removeAll();
        if (hasAvailableMetrologyConfigurations) {
            metrologyConfigurationContainer.add([
                {
                    xtype: 'combobox',
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
        var me = this;

        Ext.suspendLayouts();
        me.down('#reset-metrology-configuration').setDisabled(!newValue);
        Ext.resumeLayouts(true);
    },

    resetMetrologyConfiguration: function () {
        var me = this;

        me.down('#metrology-configuration-combo').reset();
    }
});