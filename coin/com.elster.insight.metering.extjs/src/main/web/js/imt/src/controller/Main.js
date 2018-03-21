/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Imt.controller.Main
 */
Ext.define('Imt.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.property.controller.Registry',
        'Imt.usagepointmanagement.controller.View',
        'Imt.usagepointmanagement.controller.Edit',
        'Imt.devicemanagement.controller.Device',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.devicemanagement.model.Device',              
        'Imt.dynamicprivileges.UsagePoint',
        'Imt.dynamicprivileges.Stores',
        'Imt.processes.controller.MonitorProcesses',
        'Imt.servicecalls.controller.ServiceCalls',
        'Imt.metrologyconfiguration.controller.View',
        'Imt.usagepointsetup.controller.MetrologyConfig',
        'Cfg.privileges.Validation',
        'Imt.processes.view.MetrologyConfigurationOutputs',
        'Imt.processes.view.LinkedMeterActivations',
        'Imt.processes.view.AvailableMeters',
        'Imt.processes.view.AvailableMetrologyConfigurations',
        'Imt.processes.view.PurposesOnMetrologyConfigarations',
        'Imt.processes.view.AvailableTransitions',
        'Imt.processes.view.InstallationDate',
        'Imt.processes.view.MeterRoles'
    ],

    privileges: [],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.devicemanagement.model.Device'
    ],
    controllers: [
        'Imt.controller.Dashboard',
        'Imt.dashboard.controller.OperatorDashboard',
        'Imt.dashboard.controller.FavoriteUsagePointGroups',
        'Imt.usagepointmanagement.controller.View',
        'Imt.usagepointmanagement.controller.Edit',
        'Imt.devicemanagement.controller.Device',
        'Imt.metrologyconfiguration.controller.View',
        'Imt.metrologyconfiguration.controller.Edit',
        'Imt.metrologyconfiguration.controller.ViewList',
        'Imt.controller.History',
        'Imt.controller.Search',        
        'Imt.servicecategories.controller.ServiceCategories',
        'Imt.usagepointhistory.controller.History',
        'Imt.usagepointhistory.controller.CasVersionEdit',
        'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersions',
        'Imt.processes.controller.MonitorProcesses',
        'Imt.usagepointmanagement.controller.Attributes',
        'Imt.usagepointsetup.controller.MetrologyConfig',
        'Imt.purpose.controller.Purpose',
        'Imt.purpose.controller.Readings',
        'Imt.purpose.controller.RegisterData',
        'Imt.usagepointmanagement.controller.MetrologyConfigurationDetails',
        'Imt.metrologyconfiguration.controller.ValidationConfiguration',
        'Imt.usagepointgroups.controller.AddUsagePointGroupAction',
        'Imt.metrologyconfiguration.controller.EstimationConfiguration',
        'Imt.usagepointgroups.controller.UsagePointGroups',
        'Imt.usagepointmanagement.controller.Calendars',
        'Imt.controller.SearchItemsBulkAction',
        'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
        'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
        'Imt.usagepointlifecycletransitions.controller.UsagePointLifeCycleTransitions',
        'Imt.usagepointmanagement.controller.UsagePointTransitionExecute',
        'Imt.dataquality.controller.DataQuality',
        'Imt.rulesets.controller.ValidationRuleSetPurposes',
        'Imt.rulesets.controller.AddPurposesToValidationRuleSet',
        'Imt.rulesets.controller.EstimationRuleSetPurposes',
        'Imt.rulesets.controller.AddPurposesToEstimationRuleSet',
        'Imt.usagepointmanagement.controller.ChangeUsagePointLifeCycle'
    ],
    stores: [
        'Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets',
        'Imt.usagepointmanagement.store.DefineMetrologyConfigurationPrivileges',
        'Imt.usagepointmanagement.store.UsagePointPrivileges',
        'Imt.processes.store.AvailableMeterRoles'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],
    init: function () {
        this.initHistorians();
        this.initMenu();
        this.initDynamicMenusListeners();
        this.loadCustomProperties();
        this.callParent();
    },

    initHistorians: function () {
        var me = this;

        me.getController('Imt.controller.History');
        me.getController('Imt.controller.Dashboard');
        me.getController('Cfg.controller.Validation');
        me.getController('Est.estimationrulesets.controller.EstimationRuleSets');
    },

    loadCustomProperties: function(){
        Uni.property.controller.Registry.addProperty('METROLOGYCONFIGOUTPUT', 'Imt.processes.view.MetrologyConfigurationOutputs');
        Uni.property.controller.Registry.addProperty('UP_METERACTIVATION', 'Imt.processes.view.LinkedMeterActivations');
        Uni.property.controller.Registry.addProperty('METER_MRID', 'Imt.processes.view.AvailableMeters');
        Uni.property.controller.Registry.addProperty('METROLOGYCONFIGURATION', 'Imt.processes.view.AvailableMetrologyConfigurations');
        Uni.property.controller.Registry.addProperty('METROLOGYPURPOSES', 'Imt.processes.view.PurposesOnMetrologyConfigarations');
        Uni.property.controller.Registry.addProperty('UP_TRANSITION', 'Imt.processes.view.AvailableTransitions');
        Uni.property.controller.Registry.addProperty('METER_INSTALLATION_DATE', 'Imt.processes.view.InstallationDate');
        Uni.property.controller.Registry.addProperty('METER_ROLE', 'Imt.processes.view.MeterRoles');
    },

    initDynamicMenusListeners: function () {
        var me = this;

        me.getApplication().on('validationrulesetmenurender', me.onValidationRuleSetMenuBeforeRender, me);
        me.getApplication().on('estimationRuleSetMenuRender', me.onEstimationRuleSetMenuBeforeRender, me);
    },

    onValidationRuleSetMenuBeforeRender: function (menu) {
        var me = this;

        if (Imt.privileges.MetrologyConfig.canViewValidation() || Imt.privileges.MetrologyConfig.canAdministrateValidation()) {
            menu.add(
                {
                    text: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                    itemId: 'metrology-configuration-purposes-link',
                    href: me.getController('Uni.controller.history.Router')
                        .getRoute('administration/rulesets/overview/metrologyconfigurationpurposes')
                        .buildUrl({ruleSetId: menu.ruleSetId})
                }
            );
        }
    },

    onEstimationRuleSetMenuBeforeRender: function (menu) {
        var me = this;

        if (Imt.privileges.MetrologyConfig.canViewEstimation() || Imt.privileges.MetrologyConfig.canAdministrateEstimation()) {
            menu.add(
                {
                    text: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                    itemId: 'metrology-configuration-purposes-link',
                    href: me.getController('Uni.controller.history.Router').getRoute('administration/estimationrulesets/estimationruleset/metrologyconfigurationpurposes').buildUrl()
                }
            );
        }
    },

    initMenu: function () {
    	if (Imt.privileges.UsagePoint.canAdministrate()) {
	        var menuItem = Ext.create('Uni.model.MenuItem', {
	            text: Uni.I18n.translate('general.label.usagepoints', 'IMT', 'Usage points'),
	            href: 'usagepoints',
	            portal: 'usagepoints',
	            glyph: 'usagepoints',
	            index: 20
	        });
	
	        Uni.store.MenuItems.add(menuItem);
	
	        var portalItem1 = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.usagePointLifecycleManagement', 'IMT', 'Usage point lifecycle management'),
	            portal: 'usagepoints',
	            items: [
	                {
	                    text: Uni.I18n.translate('general.label.usagepoint.add', 'IMT', 'Add usage point'),
	                    href: '#/usagepoints/add',
	                    itemId: 'add-usagepoints'
                    }
	            ]
	        });
	
	        Uni.store.PortalItems.add(
	            portalItem1
	        );

            if (Imt.privileges.UsagePointGroup.canView()) {
                Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.usagePointGroup', 'IMT', 'Usage point group'),
                    portal: 'usagepoints',
                    items: [
                        {
                            text: Uni.I18n.translate('general.usagePointGroups', 'IMT', 'Usage point groups'),
                            href: '#/usagepoints/usagepointgroups',
                            itemId: 'usage-point-groups'
                        }
                    ]
                }));
            }
    	}

        if (Imt.privileges.MetrologyConfig.canView()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.label.metrologyconfigurationManagement', 'IMT', 'Metrology configurations management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.label.metrologyconfigurations', 'IMT', 'Metrology configurations'),
                        href: '#/administration/metrologyconfiguration',
                        itemId: 'overview-metrologyconfiguration'
                    }
                ]
            }));
        }

        if (Imt.privileges.ServiceCategory.canView()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.serviceCategories', 'IMT', 'Service categories'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.serviceCategories', 'IMT', 'Service categories'),
                        href: '#/administration/servicecategories',
                        itemId: 'overview-servicecategories'
                    }
                ]
            }));
        }

        if (Imt.privileges.UsagePointLifeCycle.canView()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.usagePoint.LifecycleManagement', 'IMT', 'Usage point life cycle management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.usagePointLifeCycles', 'IMT', 'Usage point life cycles'),
                        href: '#/administration/usagepointlifecycles',
                        itemId: 'usagepointlifecycles-portal-item',
                        route: 'usagepointlifecycles'
                    }
                ]
            }));
        }

        if (Cfg.privileges.Validation.canViewResultsOrAdministerDataQuality()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'IMT', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataValidation', 'IMT', 'Data validation'),
                portal: 'workspace',
                items: [
                    {
                        text: Uni.I18n.translate('general.dataQuality', 'IMT', 'Data quality'),
                        href: '#/workspace/dataquality'
                    }
                ]
            }));
        }
    }
});
