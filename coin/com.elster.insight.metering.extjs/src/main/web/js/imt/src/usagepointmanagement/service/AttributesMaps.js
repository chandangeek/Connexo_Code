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
    technicalAttributesConfig: {
        "ELECTRICITY": {
            form : 'technical-attributes-form-electricity',
            model: 'Imt.usagepointmanagement.model.technicalinfo.Electricity'
        },
        "GAS": {
            form : 'technical-attributes-form-water',
            model: 'Imt.usagepointmanagement.model.technicalinfo.GAS'
        },
        "WATER": {
            form : 'technical-attributes-form-water',
            model: 'Imt.usagepointmanagement.model.technicalinfo.WATER'
        }
    },

    connectionStateImageMap: {
        "CONNECTED": '<span class="icon-link"></span>',
        "PHYSICALLYDISCONNECTED": '<span class="icon-link2"></span>',
        "LOGICALDISCONNECTED": '<span class="icon-link5"></span>',
        "UNKNOWN" : '<span class="icon-blocked"></span>'
    },

    typeOfUsagePointMap: {
        "UNMEASURED": Uni.I18n.translate('usagepointtypes.unmeasured', 'IMT', 'Unmeasured'),
        "SMART_DUMB": Uni.I18n.translate('usagepointtypes.smartDumb', 'IMT', 'Smart dumb'),
        "INFRASTRUCTURE": Uni.I18n.translate('usagepointtypes.infrastructure', 'IMT', 'Infrastructure'),
        "N_A": Uni.I18n.translate('usagepointtypes.notAvailable', 'IMT', 'N/A')
    },

    getForm: function (category) {
        return this.mainAttributesFormMap[category];
    },
    getServiceIcon: function (category) {
        return this.serviceCategoryImageMap[category];
    },
    getConnectionIcon: function (category) {
        return this.connectionStateImageMap[category];
    },
    getTypeOfUsagePoint: function (category) {
        return this.typeOfUsagePointMap[category];
    },
    getTechnicalAttributesConfig: function(category){
        return this.technicalAttributesConfig[category]
    }
});
