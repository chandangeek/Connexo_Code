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
                        name: 'pressure',
                        itemId: 'fld-up-pressure',
                        fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure')
                    },
                    {
                        name: 'capacity',
                        itemId: 'fld-up-capacity',
                        fieldLabel: Uni.I18n.translate('general.label.capacity', 'IMT', 'Capacity')
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
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
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
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
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
                        name: 'pressure',
                        itemId: 'fld-up-pressure',
                        fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure')
                    },
                    {
                        name: 'capacity',
                        itemId: 'fld-up-capacity',
                        fieldLabel: Uni.I18n.translate('general.label.capacity', 'IMT', 'Capacity')
                    },
                    {
                        xtype: 'checkbox',
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')

                    },
                    {
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
                    },
                    {
                        xtype: 'checkbox',
                        name: 'limiter',
                        itemId: 'fld-up-limiter',
                        fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter')
                    },
                    {
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
                    },
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