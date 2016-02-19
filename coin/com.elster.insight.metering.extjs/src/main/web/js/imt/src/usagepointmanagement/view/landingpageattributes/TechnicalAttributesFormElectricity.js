Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-electricity',


    requires: [
        'Uni.form.field.Duration'
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
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
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
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    xtype: 'textfield',
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'checkbox',
                        name: 'grounded',
                        itemId: 'up-grounded-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
                    },
                    {
                        name: 'nominalServiceVoltage',
                        itemId: 'up-service-voltage-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage')
                    },
                    {
                        name: 'phaseCode',
                        itemId: 'up-phase-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code')
                    },
                    {
                        name: 'ratedCurrent',
                        itemId: 'up-rated-current-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current')
                    },
                    {
                        name: 'ratedPower',
                        itemId: 'up-rated-power-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power')
                    },
                    {
                        name: 'estimatedLoad',
                        itemId: 'up-estimated-load-textfield',
                        fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load')
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
            return data.value + '*10<span style="position: relative;top: -6px;font-size: 10px;">' + data.multiplier + '</span> ' + data.unit;

    } else return '-';
}
});