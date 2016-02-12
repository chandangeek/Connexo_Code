Ext.define('Imt.usagepointmanagement.service.AttributesMaps', {
    singleton: true,

    serviceCategoryImageMap: {
        "ELECTRICITY": '<span class="icon-power"></span>',
        "GAS": '<span class="icon-fire2"></span>',
        "WATER": '<span class="icon-droplet"></span>'

    },

    mainAttributesFormMap: {
        "GENERAL": 'general-attributes-form',
        "ELECTRICITY": 'technical-attributes-form-electricity',
        "GAS": 'technical-attributes-form-water',
        "WATER": 'technical-attributes-form-water'
    },

    connectionStateImageMap: {

    },

    getForm: function (category) {
        return this.mainAttributesFormMap[category];
    },
    getServiceIcon: function (category) {
        return this.serviceCategoryImageMap[category];
    }
});
