/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.GridPanelOverride
 * override allows you so setup hydrator to the form.
 * You can pass hydrator class to the configyration:
 * ...
 * hydrator: 'App.example.Hydrator'
 * ...
 * or via setter:
 * form.setHydrator(hydrator);
 *
 * Once hydrator is set, data binding between form and bounded record goes through provided hydrator.
 */
Ext.define('Uni.override.FormOverride', {
    override: 'Ext.form.Basic',
    hydrator: null,

//
//    updateProperties: function () {
//        var view = this.getPropertyEdit();
//        var properties = this.propertiesStore;
//        if (properties != null) {
//            properties.each(function (property, id) {
//                    var propertyValue = Ext.create('Mdc.model.PropertyValue');
//                    var value;
//                    if (view.down('#' + property.data.key) != null) {
//                        var field = view.down('#' + property.data.key);
//                        value = field.getValue();
//
//                    if (property.getPropertyType().data.simplePropertyType === 'NULLABLE_BOOLEAN') {
//                        value = view.down('#' + property.data.key).getValue().rb;
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'DATE') {
//                        value = view.down('#date' + property.data.key).getValue();
//                        if (value !== null && value !== '') {
//                            var newDate = new Date(value.getFullYear(), value.getMonth(), value.getDate(),
//                                0, 0, 0, 0);
//                            value = value.getTime();
//                        }
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'TIMEOFDAY') {
//                        value = view.down('#time' + property.data.key).getValue();
//                        if (value !== null && value !== '') {
//                            var newDate = new Date(1970, 0, 1, value.getHours(), value.getMinutes(), value.getSeconds(), 0);
//                            value = newDate.getTime() / 1000;
//                        }
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'CLOCK') {
//                        var timeValue = view.down('#time' + property.data.key).getValue();
//                        var dateValue = view.down('#date' + property.data.key).getValue();
//                        if (timeValue !== null && timeValue !== '' && dateValue !== null && dateValue !== '') {
//                            var newDate = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate(),
//                                timeValue.getHours(), timeValue.getMinutes(), timeValue.getSeconds(), 0);
//                            value = newDate.getTime();
//                        }
//                    }
//
//                    if (property.data.isInheritedOrDefaultValue === true) {
//                        property.setPropertyValue(null);
//                    } else {
//                        propertyValue.set('value', value);
//                        property.setPropertyValue(propertyValue);
//                    }
//                }
//            );
//        }
//        return properties;
//    },

    constructor: function(owner) {
        this.callParent(arguments);
        if (owner.hydrator) {
            this.setHydrator(Ext.create(owner.hydrator))
        }
    },

    setHydrator: function(hydrator) {
        this.hydrator = hydrator
    },

    loadRecord: function(record) {
        if (!this.hydrator) {
            this.callParent(arguments)
        } else {
            this._record = record;
            return this.setValues(this.hydrator.extract(record));
        }
    },

    updateRecord: function(record) {
        record = record || this._record;

        if (this.hydrator) {
            var data = this.getFieldValues();

            record.beginEdit();
            this.hydrator.hydrate(data, record);
            record.endEdit();
            return this;
        } else {
            return this.callParent(arguments);
        }
    }
});