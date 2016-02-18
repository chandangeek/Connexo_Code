Ext.define('Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormGas', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.technical-attributes-form-gas',


    requires: [
        'Imt.usagepointmanagement.view.forms.GasInfo',
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
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    {
                        xtype: 'threevaluesdisplayfield',
                        name: 'bypass',
                        itemId: 'fld-up-bypass',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
                    },
                    {
                        name: 'bypassStatus',
                        itemId: 'fld-up-bypass-status',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass status'),
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
                        itemId: 'fld-up-load-limiter-type',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Load limiter type'),
                        listeners: {
                            beforerender: function (fld){
                                fld.setVisible(me.down('#fld-up-limiter').getValue())
                            }
                        }
                    },
                    {
                        xtype: 'measuredisplayfield',
                        name: 'LoadLimit',
                        itemId: 'fld-up-load-limit',
                        fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Load limit'),
                        unitType: 'volume',
                        listeners: {
                            beforerender: function (fld){
                                fld.setVisible(me.down('#fld-up-limiter').getValue())
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
                    },
                    {
                        xtype: 'threevaluesdisplayfield',
                        name: 'capped',
                        itemId: 'fld-up-capped',
                        fieldLabel: Uni.I18n.translate('general.label.capped', 'IMT', 'Capped')
                    },
                    {
                        name: 'clamped',
                        itemId: 'fld-up-clamped',
                        fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped'),

                    },
                    {
                        name: 'interruptible',
                        itemId: 'fld-up-interruptible',
                        fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                ]
            },
            {
                xtype: 'gas-info-form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    labelWidth: 250
                }
            },
            //{
            //    xtype: 'form',
            //    itemId: 'edit-form',
            //    hidden: true,
            //    defaults: {
            //        xtype: 'textfield',
            //        labelWidth: 250
            //    },
            //    items: [
            //        {
            //            name: 'pressure',
            //            itemId: 'fld-up-pressure',
            //            fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure')
            //        },
            //        {
            //            name: 'capacity',
            //            itemId: 'fld-up-capacity',
            //            fieldLabel: Uni.I18n.translate('general.label.capacity', 'IMT', 'Capacity')
            //        },
            //        {
            //            xtype: 'checkbox',
            //            name: 'grounded',
            //            itemId: 'fld-up-grounded',
            //            fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
            //
            //        },
            //        {
            //            name: 'bypass',
            //            itemId: 'fld-up-bypass',
            //            fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
            //        },
            //        {
            //            xtype: 'checkbox',
            //            name: 'limiter',
            //            itemId: 'fld-up-limiter',
            //            fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter')
            //        },
            //        {
            //            name: 'bypass',
            //            itemId: 'fld-up-bypass',
            //            fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
            //        },
            //    ]
            //}
        ];
        me.callParent();
    },

});