Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormWater', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-water',


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
                        name: 'pressure',
                        itemId: 'fld-up-pressure',
                        fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
                        renderer: Ext.bind(me.renderValue, me)
                    },
                    {
                        name: 'physicalCapacity',
                        itemId: 'fld-up-physicalCapacity',
                        fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
                        renderer: Ext.bind(me.renderValue, me)
                    },
                    {
                        name: 'bypassStatus',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'Valve',
                        itemId: 'fld-up-Valve',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Valve'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'clamped',
                        itemId: 'fld-up-clamped',
                        fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
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
                        name: 'pressure',
                        itemId: 'fld-up-pressure',
                        fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure')
                    },
                    {
                        name: 'physicalCapacity',
                        itemId: 'fld-up-physicalCapacity',
                        fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity')
                    },
                    {
                        name: 'bypassStatus',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status')
                    },
                    {
                        name: 'Valve',
                        itemId: 'fld-up-Valve',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Valve')
                    },
                    {
                        name: 'clamped',
                        itemId: 'fld-up-clamped',
                        fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped')
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