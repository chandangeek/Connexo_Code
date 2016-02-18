Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-electricity',


    requires: [
        'Uni.form.field.Duration',
        'Imt.usagepointmanagement.view.forms.ElectricityInfo',
        'Imt.usagepointmanagement.view.forms.fields.MeasureDisplayField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField'

    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'view-form',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    width: 600
                },
                items: [
                    {
                        xtype: 'measuredisplayfield',
                        name: 'nominalServiceVoltage',
                        itemId: 'fld-up-service-voltage',
                        fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage'),
                        unitType: 'voltage'
                    },
                    {
                        name: 'phaseCode',
                        itemId: 'fld-up-phase',
                        fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'ratedCurrent',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current'),
                        unitType: 'amperage'

                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'ratedPower',
                        itemId: 'fld-up-rated-power',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power'),
                        unitType: 'power'
                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'estimatedLoad',
                        itemId: 'fld-up-estimated-load',
                        fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
                        unitType: 'power'
                    },
                    {
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'limiter',
                        itemId: 'fld-up-limiter',
                        fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'loadLimiterType',
                        itemId: 'fld-up-loadLimiterType',
                        hidden: true,
                        fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type'),
                        listeners: {
                            beforerender: function (fld){
                                fld.setVisible(me.down('#fld-up-limiter').getValue())
                            }
                        }
                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'loadLimit',
                        itemId: 'fld-up-loadLimit',
                        fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit'),
                        unitType: 'power',
                        listeners: {
                            beforerender: function (fld){
                                fld.setVisible(me.down('#fld-up-limiter').getValue())
                            }
                        }
                    },
                    {
                        xtype: 'threevaluesdisplayfield',
                        name: 'collar',
                        itemId: 'fld-up-collar',
                        fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
                    }
                ]
            },
            {
                xtype: 'electricity-info-form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    xtype: 'textfield',
                    width: 520,
                    labelWidth: 250
                },
            },
           /* {
                xtype: 'form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    xtype: 'textfield',
                    width: 520,
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'measurefield',
                        itemId: 'nominalServiceVoltage-quantity',
                        fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage')

                    },
                    {
                        xtype: 'combobox',
                        name: 'phaseCode',
                        itemId: 'fld-up-phase',
                        fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code')
                    },
                    {
                        xtype: 'measurefield',
                        name: 'ratedCurrent',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current')
                    },
                    {
                        xtype: 'measurefield',
                        name: 'ratedPower',
                        itemId: 'fld-up-rated-power',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power')
                    },
                    {
                        xtype: 'checkbox',
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
                    },
                    {
                        xtype: 'measurefield',
                        name: 'estimatedLoad',
                        itemId: 'fld-up-estimated-load',
                        fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load')
                    },
                    {
                        name: 'estimationLoad',
                        itemId: 'fld-up-estimationLoad',
                        fieldLabel: Uni.I18n.translate('general.label.estimationLoad', 'IMT', 'Estimation load')
                    },
                    {
                        xtype: 'checkbox',
                        name: 'limiter',
                        itemId: 'fld-up-limiter',
                        fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter')
                    },
                    {
                        name: 'loadLimiterType',
                        itemId: 'fld-up-loadLimiterType',
                        fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type')
                    },
                    {
                        xtype: 'measurefield',
                        name: 'loadLimit',
                        itemId: 'fld-up-loadLimit',
                        fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit')
                    },
                    {
                        xtype: 'checkbox',
                        name: 'collar',
                        itemId: 'fld-up-collar',
                        fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
                    }
                ]
            }*/
        ];
        me.callParent();
    }
});