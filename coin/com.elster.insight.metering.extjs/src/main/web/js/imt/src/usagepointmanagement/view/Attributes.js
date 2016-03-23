Ext.define('Imt.usagepointmanagement.view.Attributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-attributes',
    requires: [
        'Imt.usagepointmanagement.model.technicalinfo.Electricity',
        'Imt.usagepointmanagement.model.technicalinfo.Gas',
        'Imt.usagepointmanagement.model.technicalinfo.Water',
        'Imt.usagepointmanagement.model.technicalinfo.Thermal',
        'Imt.usagepointmanagement.view.forms.attributes.GeneralAttributesForm',
        'Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormElectricity',
        'Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormGas',
        'Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormWater',
        'Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormThermal',
        'Imt.usagepointmanagement.view.forms.attributes.CustomAttributeSetForm'
    ],

    router: null,
    usagePoint: null,
    viewDefaults: {
        xtype: 'displayfield',
        labelWidth: 150
    },
    editDefaults: {
        labelWidth: 150,
        width: 380
    },

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
        var me = this,
            customPropertySets = me.usagePoint.customPropertySets();

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
                                title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information'),
                                record: me.usagePoint,
                                viewDefaults: me.viewDefaults,
                                editDefaults: me.editDefaults
                            },
                            {
                                xtype: me.serviceCategoryMap[me.usagePoint.get('serviceCategory')].form,
                                itemId: 'technical-attributes-form',
                                title: Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
                                record: Ext.create(me.serviceCategoryMap[me.usagePoint.get('serviceCategory')].model, me.usagePoint.get('techInfo')),
                                viewDefaults: me.viewDefaults,
                                editDefaults: me.editDefaults
                            }
                        ]
                    },
                    customPropertySets && customPropertySets.getCount() ? {
                        xtype: 'container',
                        defaults: {
                            ui: 'tile',
                            margin: '16 5 0 0'
                        },
                        items: me.prepareCpsForms(customPropertySets)
                    } : null
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
    },

    prepareCpsForms: function (customPropertySets) {
        var me = this,
            forms = [];

        customPropertySets.each(function (cps) {
            forms.push({
                xtype: 'custom-attribute-set-form',
                itemId: 'custom-attribute-set-form-' + cps.getId(),
                title: cps.get('name'),
                viewDefaults: me.viewDefaults,
                record: cps
            });
        });

        return forms;
    }
});