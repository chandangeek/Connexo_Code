Ext.define('Imt.customattributesonvaluesobjects.service.ActionMenuManager', {
    singleton: true,

    requires: [
       'Imt.customattributesonvaluesobjects.service.RouteMap'
    ],

    addAction: function (xtype, record, router, attributeSetType) {
        var actionMenusArray = Ext.ComponentQuery.query(xtype),
            route = Imt.customattributesonvaluesobjects.service.RouteMap.getRoute(attributeSetType, record.get('timesliced'), 'edit'),
            routeArguments = router.arguments;

        Ext.suspendLayouts();
        if (!record.get('timesliced') || record.get('isActive')) {
            Ext.each(actionMenusArray, function (menu) {
                menu.add({
                    itemId: 'action-menu-custom-attribute' + record.get('id'),
                    menuItemClass: 'customAttributeSet',
//                    privileges: Imt.privileges.Device.administrateDeviceData,
                    text: Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [Ext.String.htmlEncode(record.get('name'))]),
                    handler: function () {
                        routeArguments.customAttributeSetId = record.get('id');
                        if (record.get('timesliced')) {
                            routeArguments.versionId = record.get('versionId');
                        }
                        router.getRoute(route).forward(routeArguments);
                    }
                })
            });
        }
        Ext.resumeLayouts(true);
    },

    removePrevious: function (xtype) {
        var actionMenuItems = Ext.ComponentQuery.query(xtype + ' menuitem[menuItemClass=customAttributeSet]');

        Ext.each(actionMenuItems, function (item) {
            item.destroy();
        });
    }
});
