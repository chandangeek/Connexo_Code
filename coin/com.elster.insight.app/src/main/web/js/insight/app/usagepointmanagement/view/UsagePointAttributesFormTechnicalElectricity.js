Ext.define('InsightApp.usagepointmanagement.view.UsagePointAttributesFormTechnicalElectricity', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormTechnicalElectricity',

    
    requires: [
        'Uni.form.field.Duration'
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
            {xtype: 'usagePointAttributesFormMain'},
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes', 'MDC', 'Technical information'),
                itemId: 'usagePointTechnicalAttributes',
                labelAlign: 'top',
                layout: 'vbox',
                margin: '0',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.grounded', 'MDC', 'Grounded'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('usagePointManagement.yes', 'MDC', 'Yes') : Uni.I18n.translate('usagePointManagement.no', 'MDC', 'No');
                        }
                    },
                    {
                        name: 'nominalServiceVoltage',
                        itemId: 'fld-up-service-voltage',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.voltage', 'MDC', 'Nominal voltage'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'phaseCode',
                        itemId: 'fld-up-phase',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.phaseCode', 'MDC', 'Phase code'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'ratedCurrent',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.ratedCurrent', 'MDC', 'Rated current'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'ratedPower',
                        itemId: 'fld-up-rated-power',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.ratedPower', 'MDC', 'Rated power'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'estimatedLoad',
                        itemId: 'fld-up-estimated-load',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.technicalAttributes.estimatedLoad', 'MDC', 'Estimated load'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    }
                ]
            }
        ];
        me.callParent();
    },
    renderValue: function (data) {
        if (data) {
            if (data.multiplier == 0)
                return data.value + ' ' + data.unit;
            else
                return data.value + '*10<sup style="vertical-align: top; position: relative; top: -0.5em;">' + data.multiplier + '</sup> ' + data.unit;

        } else return '-';
    }
});