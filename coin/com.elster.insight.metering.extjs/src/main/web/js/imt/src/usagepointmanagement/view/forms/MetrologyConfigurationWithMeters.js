Ext.define('Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMeters', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-with-meters-info-form',
    requires: [
        'Imt.usagepointmanagement.view.StepDescription',
        'Uni.util.FormErrorMessage',
        'Uni.form.field.ComboReturnedRecordData',
        'Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMetersInfo'
    ],

    usagePoint: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'step-description',
                itemId: 'step-description',
                html: Uni.I18n.translate('usagepoint.wizard.linkMetrologyConfigurationWithMetersStep.description', 'IMT', 'Link a metrology configuration, and link meters to meter roles of the selected metrology configuration.')
            },
            {
                itemId: 'metrology-configuration-with-meters-info-warning',
                xtype: 'uni-form-error-message',
                hidden: true
            },
            {
                xtype: 'uni-form-info-message',
                itemId: 'not-all-meters-specified-message',
                text: Uni.I18n.translate('metrologyConfigurationWithMetersInfoForm.info', 'IMT', "Not all the meters are specified. The purposes of the usage point will stay in 'Incomplete' state."),
                hidden: true
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
                    width: 320
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
                itemId: 'metrology-configuration-with-meters-info',
                hidden: true,
                listeners: {
                    meterActivationsChange: Ext.bind(me.onMeterActivationsChange, me)
                }
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

    onMeterActivationsChange: function (allMetersSpecified) {
        var me = this,
            notAllMetersSpecifiedMessage = me.down('#not-all-meters-specified-message');

        notAllMetersSpecifiedMessage.setVisible(!allMetersSpecified);
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
            metrologyConfiguration.meterRoles = _.map(Ext.clone(meterActivations), function (meterActivation) {
                return Ext.merge(meterActivation.meterRole, _.pick(meterActivation, 'meter', 'activationTime'));
            });
            meterActivations = _.map(Ext.clone(meterActivations), function (meterActivation) {
                return Ext.merge(_.pick(meterActivation, 'meterRole'), {
                    meter: {
                        name: meterActivation.meter
                    }
                });
            });
        }

        return {
            metrologyConfiguration: metrologyConfiguration,
            meterActivations: meterActivations
        }
    },

    markInvalid: function (errors) {
        var me = this;

        me.getForm().markInvalid(me.mapErrors(errors));
    },

    clearInvalid: function () {
        var me = this;

        me.getForm().clearInvalid();
    },

    mapErrors: function (errors) {
        var map = {};

        Ext.Array.each(errors, function (error) {
            if (Ext.String.startsWith(error.id, 'meter.role.')) {
                error.id = 'metrologyConfiguration.meterRoles';
            }

            if (Ext.String.startsWith(error.id, 'metrologyConfiguration.purpose.')) {
                error.id = 'metrologyConfiguration.purposes';
            }

            if (!map[error.id]) {
                map[error.id] = {
                    id: error.id,
                    msg: [error.msg]
                };
            } else {
                map[error.id].msg.push(error.msg);
            }
        });

        return _.values(map);
    }
});