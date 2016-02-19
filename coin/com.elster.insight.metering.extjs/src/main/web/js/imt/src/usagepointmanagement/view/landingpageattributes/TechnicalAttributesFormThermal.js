Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormThermal', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-thermal',


    requires: [
        'Imt.usagepointmanagement.view.forms.ThermalInfo',
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
                        name: 'pressure',
                        itemId: 'fld-up-pressure',
                        fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
                        unitType: 'pressure'
                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'capacity',
                        itemId: 'fld-up-capacity',
                        fieldLabel: Uni.I18n.translate('general.label.capacity', 'IMT', 'Capacity'),
                        unitType: 'volume'
                    },
                    {
                        //xtype: 'threevaluesdisplayfield',
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
                    },
                    {
                        name: 'bypassStatus',
                        itemId: 'fld-up-bypass-status',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass status'),
                        listeners: {
                            beforerender: function (fld){
                                fld.setVisible(me.down('#fld-up-bypass').getValue())
                            }
                        }
                    },
                    {
                        xtype: 'threevaluesdisplayfield',
                        name: 'valve',
                        itemId: 'fld-up-valve',
                        fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
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
                xtype: 'thermal-info-form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    width: 520,
                    labelWidth: 250
                }
            }
        ];
        me.callParent();
    }

});