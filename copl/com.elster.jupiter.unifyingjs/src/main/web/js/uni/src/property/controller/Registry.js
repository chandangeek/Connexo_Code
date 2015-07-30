/**
 * @class Uni.property.controller.Registry
 * @singleton
 *
 * Properties registry.
 * Use it to add custom properties to the property map.
 *
 * Example:
 *
 * // add properties by one
 * var registry = Uni.property.controller.Registry;
 * registry.addProperty('CUSTOM_PROPERTY', 'App.view.property.Custom');
 *
 * // or provide a config
 * registry.addProperties({
 *      'CUSTOM_1': 'App.view.property.Custom1',
 *      'CUSTOM_2': 'App.view.property.Custom2'
 * })
 *
 */
Ext.define('Uni.property.controller.Registry', {
    extend: 'Ext.app.Controller',
    singleton: true,
    requires: [
        'Uni.property.view.property.Text',
        'Uni.property.view.property.Combobox',
        'Uni.property.view.property.Textarea',
        'Uni.property.view.property.Password',
        'Uni.property.view.property.Hexstring',
        'Uni.property.view.property.Boolean',
        'Uni.property.view.property.Number',
        'Uni.property.view.property.DecimalNumber',
        'Uni.property.view.property.NullableBoolean',
        'Uni.property.view.property.Date',
        'Uni.property.view.property.DateTime',
        'Uni.property.view.property.Period',
        'Uni.property.view.property.Time',
        'Uni.property.view.property.CodeTable',
        'Uni.property.view.property.Reference',
        'Uni.property.view.property.Multiselect',
        'Uni.property.view.property.RelativePeriod',
        'Uni.property.view.property.AdvanceReadingsSettings',
        'Uni.property.view.property.AdvanceReadingsSettingsWithoutNone',
        'Uni.property.view.property.deviceconfigurations.DeviceConfigurations'
    ],

    /**
     * Default properties registered
     */
    propertiesMap: {
        TEXT: 'Uni.property.view.property.Text',
        COMBOBOX: 'Uni.property.view.property.Combobox',
        TEXTAREA: 'Uni.property.view.property.Textarea',
        PASSWORD: 'Uni.property.view.property.Password',
        HEXSTRING: 'Uni.property.view.property.Hexstring',
        BOOLEAN: 'Uni.property.view.property.Boolean',
        NUMBER: 'Uni.property.view.property.DecimalNumber',
        LONG: 'Uni.property.view.property.Number',
        NULLABLE_BOOLEAN: 'Uni.property.view.property.NullableBoolean',
        DATE: 'Uni.property.view.property.Date',
        CLOCK: 'Uni.property.view.property.DateTime',
        TIMESTAMP: 'Uni.property.view.property.DateTime',
        TIMEDURATION: 'Uni.property.view.property.Period',
        TIMEOFDAY: 'Uni.property.view.property.Time',
        CODETABLE: 'Uni.property.view.property.CodeTable',
        REFERENCE: 'Uni.property.view.property.Reference',
        LOGBOOK: 'Uni.property.view.property.Reference',
        LOADPROFILETYPE: 'Uni.property.view.property.Reference',
        FIRMWAREVERSION: 'Uni.property.view.property.Reference',
        REGISTER: 'Uni.property.view.property.Reference',
        LOADPROFILE: 'Uni.property.view.property.Reference',
        EAN13: 'Uni.property.view.property.Text',
        EAN18: 'Uni.property.view.property.Text',
        ENCRYPTED_STRING: 'Uni.property.view.property.Password',
        UNKNOWN: 'Uni.property.view.property.Text',
        LISTVALUE: 'Uni.property.view.property.Multiselect',
        RELATIVEPERIOD: 'Uni.property.view.property.RelativePeriod',
        ADVANCEREADINGSSETTINGS: 'Uni.property.view.property.AdvanceReadingsSettings',
        ADVANCEREADINGSSETTINGSWITHOUTNONE: 'Uni.property.view.property.AdvanceReadingsSettingsWithoutNone',
        IDWITHNAME: 'Uni.property.view.property.Reference',
        DEVICECONFIGURATIONLIST: 'Uni.property.view.property.deviceconfigurations.DeviceConfigurations'
    },

// store must be registered on some ctrl (not in the responsibility of this class: move later?)
    stores: [
        'Uni.property.store.TimeUnits',
        'Uni.property.store.RelativePeriods',
        'Uni.property.store.PropertyReadingTypes',
        'Uni.property.store.PropertyDeviceConfigurations'
    ],

    /**
     * Register new property
     *
     * @param {string} key
     * @param {string} model
     */
    addProperty: function (key, model) {
        if (!Ext.isString(key)) {
            throw '!Ext.isString(key)'
        }

        if (!this.getProperty(key)) {
            this.propertiesMap[key] = model;
        }
    },

    /**
     * Register properties config
     *
     * @param {Object} properties
     */
    addProperties: function (properties) {
        Ext.apply(this.propertiesMap, properties)
    },

    /**
     * Retrieve property widget
     * @see Uni.property.view.property.Base
     *
     * @param {string} key
     * @returns {string|null}
     */
    getProperty: function (key) {
        return this.propertiesMap[key] || null;
    }
})
;