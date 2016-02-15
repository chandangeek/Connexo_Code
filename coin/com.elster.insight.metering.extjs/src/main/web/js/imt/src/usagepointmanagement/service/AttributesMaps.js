Ext.define('Imt.usagepointmanagement.service.AttributesMaps', {
    singleton: true,

    serviceCategoryImageMap: {
        "ELECTRICITY": '<span class="icon-power"></span>',
        "GAS": '<span class="icon-fire2"></span>',
        "WATER": '<span class="icon-droplet"></span>',
        "THERMAL": '<span class="icon-rating3"></span>'

    },

    mainAttributesFormMap: {
        "GENERAL": 'general-attributes-form',
        "ELECTRICITY": 'technical-attributes-form-electricity',
        "GAS": 'technical-attributes-form-water',
        "WATER": 'technical-attributes-form-water'
    },

    connectionStateImageMap: {
        "CONNECTED": '<span class="icon-link"></span>',
        "PHYSICALLY DISCONNECTED": '<span class="icon-link2"></span>',
        "LOGICAL DISCONNECTED": '<span class="icon-link5"></span>',
        "UNKNOWN" : '<span class="icon-blocked"></span>'
    },

    getForm: function (category) {
        return this.mainAttributesFormMap[category];
    },
    getServiceIcon: function (category) {
        return this.serviceCategoryImageMap[category];
    },
    getConnectionIcon: function (category) {
        return this.connectionStateImageMap[category];
    }
});
