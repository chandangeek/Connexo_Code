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
        anchor: '100%',
        maxWidth: 421
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
            dynamicElements = me.prepareDynamicElements();

        me.canManageUsagePoint = Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.manageAttributes);

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
                        xtype: 'uni-button-action',
                        itemId: 'usage-point-attributes-actions-button',
                        privileges: me.canManageUsagePoint,
                        usagePoint: me.usagePoint,
                        margin: '0 16 0 0',
                        menu: {
                            xtype: 'menu',
                            itemId: 'usage-point-attributes-actions-menu',
                            plain: true,
                            items: dynamicElements.menuItems
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        defaults: {
                            ui: 'tile2'
                        },
                        items: [
                            {
                                xtype: 'general-attributes-form',
                                itemId: 'general-attributes-form',
                                title: Uni.I18n.translate('general.generalInformation', 'IMT', 'General information'),
                                router: me.router,
                                record: me.usagePoint,
                                viewDefaults: me.viewDefaults,
                                editDefaults: me.editDefaults,
                                hasEditMode: me.canManageUsagePoint
                            },
                            {
                                xtype: me.serviceCategoryMap[me.usagePoint.get('serviceCategory')].form,
                                itemId: 'technical-attributes-form',
                                title: Uni.I18n.translate('general.technicalInformation', 'IMT', 'Technical information'),
                                record: Ext.create(me.serviceCategoryMap[me.usagePoint.get('serviceCategory')].model, me.usagePoint.get('techInfo')),
                                viewDefaults: me.viewDefaults,
                                editDefaults: me.editDefaults,
                                hasEditMode: me.canManageUsagePoint
                            }
                        ]
                    },
                    dynamicElements.forms.length ? {
                        xtype: 'container',
                        defaults: {
                            ui: 'tile2'
                        },
                        items: dynamicElements.forms
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

    prepareDynamicElements: function () {
        var me = this,
            forms = [],
            menuItems = me.canManageUsagePoint ? [
                {
                    text: Uni.I18n.translate('general.editGeneralInformation', 'IMT', "Edit 'General information'"),
                    itemId: 'edit-general-attributes',
                    linkedForm: 'general-attributes-form'
                },
                {
                    text: Uni.I18n.translate('general.editTechnicalInformation', 'IMT', "Edit 'Technical information'"),
                    itemId: 'edit-technical-attributes',
                    linkedForm: 'technical-attributes-form'
                }
            ] : [];

        me.usagePoint.customPropertySets().each(function (cps) {
            var customPropertySetId =cps.get('customPropertySetId'),
                itemId = 'custom-attribute-set-form-' + customPropertySetId,
                name = cps.get('name'),
                hasEditMode = me.canManageUsagePoint
                    && cps.get('isEditable')
                    && (!cps.get('isVersioned') || cps.get('isActive'));

            forms.push({
                xtype: 'custom-attribute-set-form',
                itemId: itemId,
                title: name,
                viewDefaults: me.viewDefaults,
                editDefaults: {
                    labelWidth: me.editDefaults.labelWidth,
                    width: 191,
                    hasNotValueSameAsDefaultMessage: true
                },
                record: cps,
                hasEditMode: hasEditMode,
                router: me.router
            });

            if (hasEditMode && me.canManageUsagePoint) {
                menuItems.push({
                    text: Uni.I18n.translate('general.editX', 'IMT', "Edit '{0}'", [name]),
                    itemId: 'edit-custom-attribute-set-' + customPropertySetId,
                    linkedForm: itemId
                });
            }
        });

        return {
            forms: forms,
            menuItems: menuItems
        };
    }
});