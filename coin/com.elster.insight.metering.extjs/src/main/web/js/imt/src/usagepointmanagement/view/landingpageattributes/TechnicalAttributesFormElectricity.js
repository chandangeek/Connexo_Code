Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-electricity',


    requires: [
        'Uni.form.field.Duration',
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150,
        xtype: 'displayfield'
    },

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
                        name: 'nominalServiceVoltage',
                        itemId: 'fld-up-service-voltage',
                        fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage'),
                        renderer: Ext.bind(me.renderValue, me)

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
                        name: 'ratedCurrent',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current'),
                        renderer: Ext.bind(me.renderValue, me)
                    },
                    {
                        name: 'ratedPower',
                        itemId: 'fld-up-rated-power',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power'),
                        renderer: Ext.bind(me.renderValue, me)
                    },
                    {
                        name: 'estimatedLoad',
                        itemId: 'fld-up-estimated-load',
                        fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
                        renderer: Ext.bind(me.renderValue, me)
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
                        name: 'estimationLoad',
                        itemId: 'fld-up-estimationLoad',
                        fieldLabel: Uni.I18n.translate('general.label.estimationLoad', 'IMT', 'Estimation load'),
                        renderer: Ext.bind(me.renderValue, me)
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
                        fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type')
                    },
                    {
                        name: 'loadLimit',
                        itemId: 'fld-up-loadLimit',
                        fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit'),
                        renderer: Ext.bind(me.renderValue, me)
                    },
                    {
                        name: 'collar',
                        itemId: 'fld-up-collar',
                        fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
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
    },
    renderValue: function (data) {
        if (data) {
          if (data.multiplier == 0)
              return data.value + ' ' + data.unit;
           else
               return data.value + '*10<span style="position: relative;top: -6px;font-size: 10px;">' + data.multiplier + '</span> ' + data.unit;

        } else return '-';
    }
});