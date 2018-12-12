/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.service.VersionsManager', {
    singleton: true,

    requires: [
        'Imt.customattributesonvaluesobjects.service.RouteMap'
    ],

    addVersion: function (record, container, router, attributeSetType, propertyForm, inline) {
        var htmlString = '',
            versionRoute = Imt.customattributesonvaluesobjects.service.RouteMap.getRoute(attributeSetType, true, 'version'),
            routeArguments = router.arguments,
            routeQueryParams = router.queryParams;

        if (record.get('isActive')) {
            if (record.get('startTime')) {
                htmlString += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'IMT', 'From'), Uni.DateTime.formatDateTimeLong(new Date(record.get('startTime'))));
            }
            if (record.get('startTime') && record.get('endTime')) {
                htmlString += ' - ';
            }
            if (record.get('endTime')) {
                htmlString += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'IMT', 'Until'), Uni.DateTime.formatDateTimeLong(new Date(record.get('endTime'))));
            }
            if (!record.get('endTime') && !record.get('startTime')) {
                htmlString += Uni.I18n.translate('general.infinite', 'IMT', 'Infinite');
            }
            propertyForm.loadRecord(record);
        } else {
            htmlString = '(' + Uni.I18n.translate('customattributesets.versions.none', 'IMT', 'no active version') + ')';
        }

        Ext.suspendLayouts();
        container.add({
                xtype: 'container',
                html: '<span style="font-family: Lato, helvetica, arial, verdana, sans-serif; font-style: normal; color: #686868;">' + htmlString + '</span>'

            });

        if (!inline) {
            container.add({
                xtype: 'button',
                ui: 'link',
                text: Uni.I18n.translate('customattributesets.versions', 'IMT', 'Versions'),
                margin: '-5 0 0 5',
                handler: function () {
                    if (attributeSetType === 'device') {
                        routeQueryParams.customAttributeSetId = record.get('id');
                    } else {
                        routeArguments.customAttributeSetId = record.get('id');
                    }

                    router.getRoute(versionRoute).forward(routeArguments, routeQueryParams);
                }
            });
        }

        Ext.resumeLayouts(true);
    }
});
