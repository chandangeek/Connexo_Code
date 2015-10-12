Ext.define('Mdc.customattributesonvaluesobjects.service.RouteMap', {
    singleton: true,

        routeMap: {
            "device": "devices/device/attributes",
            "channel": "devices/device/channels/channel/editcustomattributes",
            "register": "devices/device/registers/register/editcustomattributes"
        },

        getRoute: function (type) {
            return this.routeMap[type];
        }
});
