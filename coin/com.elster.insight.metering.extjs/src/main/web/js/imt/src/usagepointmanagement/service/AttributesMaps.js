Ext.define('Imt.usagepointmanagement.service.AttributesMaps', {
    singleton: true,

    serviceCategoryImageMap: {
        "ELECTRICITY": '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px; background-image: url(../imt/resources/images/flash.png)" ></span>',
        "GAS": '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px; background-image: url(../imt/resources/images/flame.png)" ></span>',
        "WATER": '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px; background-image: url(../imt/resources/images/drop.png)" ></span>'
    },

    serviceCategoryTechnicalAttributesFormMap: {
        "GENERAL": 'general-attributes-form',
        "ELECTRICITY": 'technical-attributes-form-electricity'
    },

    connectionStateImageMap: {

    },

    getForm: function (category) {
        return this.serviceCategoryTechnicalAttributesFormMap[category];
    },
    getServiceIcon: function (category) {
        return this.serviceCategoryImageMap[category];
    }
});
