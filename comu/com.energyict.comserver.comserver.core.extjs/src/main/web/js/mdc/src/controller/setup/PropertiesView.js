/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @deprecated
 */
Ext.define('Mdc.controller.setup.PropertiesView', {
    extend: 'Ext.app.Controller',

    stores: [
        'TimeUnits'
    ],

    requires: [
        'Uni.property.model.Property',
        'Mdc.view.setup.property.PropertyView',
        'Mdc.store.TimeUnits'
    ],

    models: [
        'Uni.property.model.Property'
    ],

    views: [
        'setup.property.PropertyView'

    ],

    refs: [
        {
            ref: 'propertyView',
            selector: 'viewport #propertyView'
        }
    ],

    init: function () {

    },

    showProperties: function (objectWithProperties, view) {
        var me = this;
        var propertiesView = view.down('#propertyView');
        var column0View = view.down('#propertyColumn0');
        column0View.removeAll();
        var column1View = view.down('#propertyColumn1');
        column1View.removeAll();

        var properties = objectWithProperties.propertiesStore.data.items;
        var propertyNumber = 0;

        properties.forEach(function (entry) {
                var property = entry;
                var propertyType = property.getPropertyType().data.simplePropertyType;
                var propertyValue = null;
                var value = null;
                var key = property.data.key;
                propertyNumber++;
                var columnNumber = propertyNumber % 2;

                try {
                    propertyValue = property.getPropertyValue();

                    if ((propertyValue != null)) {
                        value = propertyValue.data.value;
                        if (value === '') {
                            value = propertyValue.data.inheritedValue;
                        }
                        if (value === '') {
                            value = propertyValue.data.defaultValue;
                        }
                    }
                } catch (ex) {
                }

                switch (propertyType) {
                    case 'TEXT':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'TEXTAREA':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'PASSWORD':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'HEXSTRING':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'BOOLEAN':
                        if (value === true) {
                            propertiesView.addProperty(key, 'Yes', columnNumber);
                        } else {
                            propertiesView.addProperty(key, 'No', columnNumber);
                        }
                        break;
                    case 'NULLABLE_BOOLEAN':
                        if (value === true) {
                            propertiesView.addProperty(key, 'Yes', columnNumber);
                        } else if (value === false) {
                            propertiesView.addProperty(key, 'No', columnNumber);
                        } else {
                            propertiesView.addProperty(key, 'N/A', columnNumber);
                        }
                        break;
                    case 'NUMBER':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'CLOCK':
                        if (value !== null && value !== '') {
                            var date = new Date(value);
                            propertiesView.addProperty(key, Uni.DateTime.formatDateTimeLong(date), columnNumber);
                        } else {
                            propertiesView.addProperty(key, value, columnNumber);
                        }
                        break;
                    case 'DATE':
                        if (value !== null) {
                            propertiesView.addProperty(key, new Date(value).toLocaleDateString(), propertyNumber % 2);
                        } else {
                            propertiesView.addDateProperty(key, null, columnNumber);
                        }
                        break;
                    case 'TIMEDURATION':
                        var unit;
                        var count;
                        var timeDuration = null;
                        if (value != null) {
                            unit = value.timeUnit;
                            count = value.count;
                            timeDuration = count + ' ' + unit;
                        }
                        propertiesView.addProperty(key, timeDuration, columnNumber);
                        break;
                    case 'TIMEOFDAY':
                        if (value !== null) {
                            propertiesView.addProperty(key, new Date(value).toLocaleTimeString(), columnNumber);
                        } else {
                            propertiesView.addProperty(key, null, propertyNumber % 2);
                        }
                        break;
                    case 'CODETABLE':
                        if (value !== null) {
                            propertiesView.addProperty(key, value.codeTableId + '-' + value.name, columnNumber);
                        } else {
                            propertiesView.addProperty(key, null, columnNumber);
                        }
                        break;
                    case 'LOADPROFILETYPE':
                        if (value !== null) {
                            propertiesView.addProperty(key, value.loadProfileTypeId + '-' + value.name, columnNumber);
                        } else {
                            propertiesView.addProperty(key, null, columnNumber);
                        }
                        break;
                    case 'REFERENCE':
                        properties.addProperty(key, value, columnNumber);
                        break;
                    case 'EAN13':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'EAN18':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'USERFILEREFERENCE':

                        if (value !== null) {
                            propertiesView.addProperty(key, value.userFileReferenceId + '-' + value.name, propertyNumber % 2);
                        } else {
                            propertiesView.addProperty(key, null, columnNumber);
                        }
                        break;
                    case 'OBISCODE':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'SPATIAL_COORDINATES':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'ENCRYPTED_STRING':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                    case 'UNKNOWN':
                        propertiesView.addProperty(key, value, columnNumber);
                        break;
                }
            }
        )
    }
});