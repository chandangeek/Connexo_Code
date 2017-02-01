/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.service.RouteMap', {
    singleton: true,

    routeMap: {
        "device": "devices/device/attributes/editcustomattributes",
        "channel": "devices/device/channels/channel/editcustomattributes",
        "register": "devices/device/registers/register/editcustomattributes"
    },

    timeSlicedRouteMapEdit: {
        "device": "devices/device/history/customattributesversionsedit",
        "channel": "devices/device/channels/channel/customattributesversions/edit",
        "register": "devices/device/registers/register/customattributesversions/edit"
    },

    timeSlicedRouteMapAdd: {
        "device": "devices/device/history/customattributesversionsadd",
        "channel": "devices/device/channels/channel/customattributesversions/add",
        "register": "devices/device/registers/register/customattributesversions/add"
    },

    timeSlicedRouteMapClone: {
        "device": "devices/device/history/customattributesversionsclone",
        "channel": "devices/device/channels/channel/customattributesversions/clone",
        "register": "devices/device/registers/register/customattributesversions/clone"
    },

    timeSlicedRouteMapVersion: {
        "device": "devices/device/history",
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
