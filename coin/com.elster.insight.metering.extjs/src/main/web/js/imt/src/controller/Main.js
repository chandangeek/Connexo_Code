/**
 * @class Imt.controller.Main
 */
Ext.define('Imt.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
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
        'Imt.usagepointsetup.controller.MetrologyConfig'
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
        'Imt.rulesets.controller.ValidationRuleSetPurposes',
        'Imt.rulesets.controller.AddPurposesToValidationRuleSet',
        'Imt.rulesets.controller.EstimationRuleSetPurposes',
        'Imt.rulesets.controller.AddPurposesToEstimationRuleSet'
    ],
    stores: [
        'Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets',
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
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
        this.callParent();
    },

    initHistorians: function () {
        this.getController('Imt.controller.History');
        this.getController('Imt.controller.Dashboard');
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
    }
});
