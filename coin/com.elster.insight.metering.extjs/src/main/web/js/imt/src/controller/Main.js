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
        'Imt.usagepointmanagement.controller.MetrologyConfigurationDetails',
        'Imt.metrologyconfiguration.controller.ValidationConfiguration'
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
	            title: Uni.I18n.translate('general.label.administration', 'IMT', 'Administration'),
	            portal: 'usagepoints',
	            items: [
	                {
	                    text: Uni.I18n.translate('general.label.usagepoint.add', 'IMT', 'Add usage point'),
	                    href: '#/usagepoints/add',
	                    itemId: 'add-usagepoints'
	                },
	            ]
	        });
	
	        Uni.store.PortalItems.add(
	            portalItem1
	        );
    	}

        if (Imt.privileges.MetrologyConfig.canView()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.label.metrologyconfigurationManagement', 'IMT', 'Metrology configurations management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configurations'),
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
    }
});
