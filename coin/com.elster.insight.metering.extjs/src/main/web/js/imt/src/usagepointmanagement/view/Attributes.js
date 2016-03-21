Ext.define('Imt.usagepointmanagement.view.Attributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-attributes',
    requires: [
        'Imt.usagepointmanagement.view.landingpageattributes.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormGas',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormWater',
        'Imt.usagepointmanagement.view.landingpageattributes.TechnicalAttributesFormThermal'
    ],

    router: null,
    usagePoint: null,

    serviceCategoryMap: {
        'ELECTRICITY': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Electricity',
            form: 'technical-attributes-form-electricity'
        },
        'GAS': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Gas',
            form: 'technical-attributes-form-gas'
        },
        'WATER': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Water',
            form: 'technical-attributes-form-water'
        },
        'HEAT': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Thermal',
            form: 'technical-attributes-form-thermal'
        }
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'usage-point-content',
                title: Uni.I18n.translate('general.usagePointAttributes', 'IMT', 'Usage point attributes'),
                ui: 'large',
                layout: 'hbox',
                defaults: {
                    flex: 1
                },
                tools: [
                    {
                        xtype: 'button',
                        itemId: 'usage-point-actions',
                        text: Uni.I18n.translate('general.actions','IMT','Actions'),
                        privileges: Imt.privileges.UsagePoint.admin,
                        iconCls: 'x-uni-action-iconD',
                        usagePoint: me.usagePoint
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        defaults: {
                            ui: 'tile',
                            margin: '16 16 0 0'
                        },
                        items: [
                            {
                                xtype: 'general-attributes-form',
                                itemId: 'general-attributes-form',
                                title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information')
                            },
                            {
                                xtype: me.serviceCategoryMap[me.usagePoint.get('serviceCategory')].form,
                                itemId: 'technical-attributes-form',
                                title: Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information')
                            }
                        ]
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.loadUsagePoint(me.usagePoint);
    },

    loadUsagePoint: function (usagePoint) {
        var me = this;

        if (usagePoint) {
            //me.down('#usage-point-summary').loadRecord(usagePoint);
            //me.down('#usage-point-metrology-config').loadRecord(new Imt.usagepointmanagement.model.MetrologyConfigOnUsagePoint(usagePoint.get('metrologyConfiguration')));
            me.usagePoint = usagePoint;
        }
    }
});