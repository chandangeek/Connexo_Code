/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.service.RouteMap', {
    singleton: true,

    routeMap: {
        "usagePoint": "usagepoints/view/attributes/editcustomattributes",
        "channel": "devices/device/channels/channel/editcustomattributes",
        "register": "devices/device/registers/register/editcustomattributes",
        "up": "usagepoints/view"
    },

    timeSlicedRouteMapEdit: {
        "usagePoint": "usagepoints/view/history/customattributesversionsedit",
        "channel": "devices/device/channels/channel/customattributesversions/edit",
        "register": "devices/device/registers/register/customattributesversions/edit"
    },

    timeSlicedRouteMapAdd: {
        "usagePoint": "usagepoints/view/history/customattributesversionsadd",
        "channel": "devices/device/channels/channel/customattributesversions/add",
        "register": "devices/device/registers/register/customattributesversions/add"
    },

    timeSlicedRouteMapClone: {
        "usagePoint": "usagepoints/view/history/customattributesversionsclone",
        "channel": "devices/device/channels/channel/customattributesversions/clone",
        "register": "devices/device/registers/register/customattributesversions/clone"
    },

    timeSlicedRouteMapVersion: {
        "usagePoint": "usagepoints/view/history",
        "channel": "devices/device/channels/channel/customattributesversions",
        "register": "devices/device/registers/register/customattributesversions"
    },

    getRoute: function (type, timesliced, action) {
        if (!timesliced) {
            return this.routeMap[type];
        } else {
            switch (action) {
                case 'add':
                    return this.timeSlicedRouteMapAdd[type];
                    break;
                case 'clone':
                    return this.timeSlicedRouteMapClone[type];
                    break;
                case 'edit':
                    return this.timeSlicedRouteMapEdit[type];
                    break;
                case 'version':
                    return this.timeSlicedRouteMapVersion[type];
                    break;

            }
        }
    }
});
