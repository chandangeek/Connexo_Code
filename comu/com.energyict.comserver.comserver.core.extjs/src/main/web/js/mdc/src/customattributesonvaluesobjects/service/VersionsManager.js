/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.service.VersionsManager', {
    singleton: true,

    requires: [
        'Mdc.customattributesonvaluesobjects.service.RouteMap'
    ],

    addVersion: function (record, container, router, attributeSetType, propertyForm) {
        var htmlString = '',
            versionRoute = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(attributeSetType, true, 'version'),
            routeArguments = router.arguments,
            routeQueryParams = router.queryParams;

        if (record.get('isActive')) {
            if (record.get('startTime')) {
                htmlString += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'MDC', 'From'), Uni.DateTime.formatDateTimeLong(record.get('startTime')));
            }
            if (record.get('startTime') && record.get('endTime')) {
                htmlString += ' - ';
            }
            if (record.get('endTime')) {
                htmlString += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'MDC', 'Until'), Uni.DateTime.formatDateTimeLong(record.get('endTime')));
            }
            if (!record.get('endTime') && !record.get('startTime')) {
                htmlString += Uni.I18n.translate('general.infinite', 'MDC', 'Infinite');
            }else{
                htmlString = '<span data-qtip="' + Ext.String.htmlEncode(htmlString) + '">' + htmlString + '</span>';
            }
            propertyForm.loadRecord(record);
        } else {
            htmlString = '(' + Uni.I18n.translate('customattributesets.versions.none', 'MDC', 'no active version') + ')';
        }

        Ext.suspendLayouts();
        container.add([
            {
                xtype: 'container',
                html: '<span style="font-family: Lato, helvetica, arial, verdana, sans-serif; font-style: normal; color: #686868;">' + htmlString + '</span>'

            },
            {
                xtype: 'button',
                ui: 'link',
                text: Uni.I18n.translate('customattributesets.versions', 'MDC', 'Versions'),
                margin: '-5 0 0 5',
                handler: function () {
                    if (attributeSetType === 'device') {
                        routeQueryParams.customAttributeSetId = record.get('id');
                    } else {
                        routeArguments.customAttributeSetId = record.get('id');
                    }

                    router.getRoute(versionRoute).forward(routeArguments, routeQueryParams);
                }
            }
        ]);
        Ext.resumeLayouts(true);
    }
});
