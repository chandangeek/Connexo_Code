/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @deprecated
 */
Ext.define('Mdc.controller.setup.Properties', {
    extend: 'Ext.app.Controller',

    stores: [
        'TimeUnits'
    ],

    requires: [
        'Mdc.store.CodeTables',
        'Mdc.store.UserFileReferences',
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.TimeUnits',
        'Uni.property.model.Property',
        'Uni.property.model.PossibleValue',
        'Mdc.view.setup.property.Edit'
    ],

    models: [
        'Uni.property.model.Property'
    ],

    views: [
        'setup.property.Edit',
        'setup.property.CodeTable',
        'setup.property.UserFileReference',
        'setup.property.LoadProfileType'
    ],
    refs: [
        {
            ref: 'propertyEdit',
            selector: 'viewport #propertyEdit'
        },
        {
            ref: 'codeTableSelectionGrid',
            selector: '#codeTableSelectionGrid'
        },
        {
            ref: 'userFileReferenceSelectionGrid',
            selector: '#userFileReferenceSelectionGrid'
        },
        {
            ref: 'loadProfileTypeSelectionGrid',
            selector: '#loadProfileTypeSelectionGrid'
        },
        {
            ref: 'propertiesForm',
            selector: '#propertiesform'
        },
        {
            ref: 'restoreAllButton',
            selector: '#restoreAllButton'
        }
    ],
    timeDurationStore: Ext.create('Ext.data.Store', {
        fields: [
            {name: 'key', type: 'string'},
            {name: 'value', type: 'string'}
        ]
    }),

    codeTableSelectionWindow: null,
    userFileReferenceSelectionWindow: null,
    buttonClicked: null,
    propertiesStore: null,
    hidden: true,

    init: function () {
        this.control({
            'propertyEdit button[action=showCodeTable]': {
                click: this.showCodeTableOverview
            },
            'propertyEdit button[action=showUserFileReference]': {
                click: this.showUserFileReferenceOverview
            },
            'propertyEdit button[action=showLoadProfileType]': {
                click: this.showLoadProfileTypeOverview
            },
            'propertyEdit button[action=delete]': {
                click: this.restoreDefaultProperty
            },
            'propertyEdit button[action=reset]': {
                click: this.resetValue
            },
            'codeTableSelectionWindow button[action=cancel]': {
                click: this.closeCodeTableSelectionWindow
            },
            'codeTableSelectionWindow button[action=select]': {
                click: this.selectCodeTable
            },
            'userFileReferenceSelectionWindow button[action=cancel]': {
                click: this.closeUserFileReferenceSelectionWindow
            },
            'userFileReferenceSelectionWindow button[action=select]': {
                click: this.selectUserFileReference
            },
            'loadProfileTypeSelectionWindow button[action=cancel]': {
                click: this.closeLoadProfileTypeSelectionWindow
            },
            'loadProfileTypeSelectionWindow button[action=select]': {
                click: this.selectLoadProfileType
            },
            'propertyEdit textfield': {
                change: this.changeProperty
            },
            'propertyEdit radio': {
                change: this.changeRadioGroupProperty
            },
            'propertyEdit checkbox[cls=check]': {
                change: this.changeProperty
            }
        });
    },

    showPropertiesAsInherited: function (objectWithProperties, view, hidden) {
        var me = this;
        var properties = objectWithProperties.propertiesStore.data.items;
        this.propertiesStore = objectWithProperties.propertiesStore;
        properties.forEach(function(property){
            try {
                var propertyValue = property.getPropertyValue();
                propertyValue.data.inheritedValue = propertyValue.data.value;
                propertyValue.data.value = '';
            } catch(e) {}
        });
        this.show(properties, view, hidden);
    },

    showProperties: function (objectWithProperties, view, hidden) {
        var properties = objectWithProperties.propertiesStore.data.items;
        this.propertiesStore = objectWithProperties.propertiesStore;
        this.show(properties, view, hidden);
    },

    show: function (properties, view, hidden) {
        var me = this;
        var propertiesView = view.down('#propertyEdit');
        this.hidden = hidden;
        var propertiesForm = view.down('#propertiesform');
        var items = propertiesForm.items.items.slice(0);
        Ext.each(items, function (child, index) {
            propertiesForm.remove(child);
        });


        properties.forEach(function (entry) {
                var property = entry;
                var propertyType = property.getPropertyType().data.simplePropertyType;
                var propertyValue = null;
                var value = null;
                var key = property.data.key;
                var isInheritedValue = true;
                var propertyValidationRule = null;
                var restoreValue = '';

                try {
                    propertyValue = property.getPropertyValue();

                    if ((propertyValue != null)) {
                        value = propertyValue.data.value;
                        isInheritedValue = false;
                        if (value === '') {
                            value = propertyValue.data.inheritedValue;
                            isInheritedValue = true;
                        }
                        if (value === '') {
                            value = propertyValue.data.defaultValue;
                            isInheritedValue = true;
                        }
                        if (propertyValue.data.inheritedValue !== '') {
                            restoreValue = propertyValue.data.inheritedValue;
                        } else {
                            restoreValue = propertyValue.data.defaultValue;
                        }
                    }
                } catch (ex) {
                }

                property.data.isInheritedOrDefaultValue = isInheritedValue;
                me.propertiesStore.commitChanges();

                try {
                    var predefinedPropertyValues = property.getPropertyType().getPredefinedPropertyValue().data.possibleValues;
                    var selectionMode = property.getPropertyType().getPredefinedPropertyValue().data.selectionMode;
                    var exhaustive = property.getPropertyType().getPredefinedPropertyValue().data.exhaustive;
                } catch (ex) {

                }

                try {
                    propertyValidationRule = property.getPropertyType().getPropertyValidationRule();
                } catch (ex) {

                }

                var required = property.data.required;

                switch (propertyType) {
                    case 'TEXT':
                        if (selectionMode === 'COMBOBOX') {
                            propertiesView.addComboBoxTextProperty(key, predefinedPropertyValues, value, exhaustive, restoreValue, required);
                        } else {
                            propertiesView.addTextProperty(key, value, restoreValue, required);
                        }
                        break;
                    case 'TEXTAREA':
                        propertiesView.addTextAreaProperty(key, value, restoreValue, required);
                        break;
                    case 'PASSWORD':
                        propertiesView.addPasswordProperty(key, value, restoreValue, required);
                        break;
                    case 'HEXSTRING':
                        propertiesView.addHexStringProperty(key, value, restoreValue, required);
                        break;
                    case 'BOOLEAN':
                        if (value === true) {
                            propertiesView.addBooleanProperty(key, true, restoreValue, required);
                        } else {
                            propertiesView.addBooleanProperty(key, false, restoreValue, required);
                        }
                        break;
                    case 'NULLABLE_BOOLEAN':
                        if (value === true) {
                            propertiesView.addNullableBooleanProperty(key, true, false, false, restoreValue, required);
                        } else if (value === false) {
                            propertiesView.addNullableBooleanProperty(key, false, true, false, restoreValue, required);
                        } else {
                            propertiesView.addNullableBooleanProperty(key, false, false, true, restoreValue, required);
                        }

                        break;
                    case 'NUMBER':
                        if (selectionMode === 'COMBOBOX') {
                            propertiesView.addComboBoxNumberProperty(key, predefinedPropertyValues, parseFloat(value), exhaustive, restoreValue, required);
                        } else {
                            var allowDecimals = true;
                            if (propertyValidationRule != null) {
                                var minValue = propertyValidationRule.data.minimumValue;
                                var maxValue = propertyValidationRule.data.maximumValue;
                                allowDecimals = propertyValidationRule.data.allowDecimals;
                            }
                            propertiesView.addNumberProperty(key, value, minValue, maxValue, allowDecimals, restoreValue, required);
                        }
                        break;
                    case 'CLOCK':
                        if (value !== null && value !== '') {
                            var date = new Date(value);
                            var dateValue = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
                            var timeValue = new Date(1970, 0, 1, date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                            propertiesView.addDateTimeProperty(key, dateValue, timeValue, restoreValue, required);
                        } else {
                            propertiesView.addDateTimeProperty(key, null, null, restoreValue, required);
                        }
                        break;
                    case 'DATE':
                        if (value !== null) {
                            propertiesView.addDateProperty(key, new Date(value), restoreValue, required);
                        } else {
                            propertiesView.addDateProperty(key, null, restoreValue, required);
                        }
                        break;
                    case 'TIMEDURATION':
                        var unit;
                        var count;
                        var timeDuration = null;
                        if (value != null) {
                            //var durationValue = moment.duration(value.seconds, 'seconds').humanize();
                            unit = value.timeUnit;
                            count = value.count;
                            timeDuration = count + ' ' + unit;
                        }

                        if (selectionMode === 'COMBOBOX') {
                            //clear store
                            me.timeDurationStore.loadData([], false);
                            for (var i = 0; i < predefinedPropertyValues.length; i++) {
                                //var timeDuration = moment.duration(predefinedPropertyValues[i].seconds, 'seconds').humanize();
                                //me.timeDurationStore.add({key: predefinedPropertyValues[i].seconds, value: timeDuration})
                                var timeDurationValue = predefinedPropertyValues[i].count + " " + predefinedPropertyValues[i].timeUnit;
                                var timeDurationKey = predefinedPropertyValues[i].count + " " + predefinedPropertyValues[i].timeUnit;
                                me.timeDurationStore.add({key: timeDurationKey, value: timeDurationValue});
                            }
                            propertiesView.addComboBoxTextProperty(key, me.timeDurationStore, timeDuration, exhaustive, restoreValue, required);
                        } else {
                            propertiesView.addTimeDurationProperty(key, count, unit, me.getTimeUnitsStore(), restoreValue, required);
                        }
                        break;
                    case 'TIMEOFDAY':
                        if (value !== null) {
                            propertiesView.addTimeProperty(key, new Date(value * 1000), restoreValue, required);
                        } else {
                            propertiesView.addTimeProperty(key, null, restoreValue, required);
                        }
                        break;
                    case 'CODETABLE':
                        if (value !== null) {
                            propertiesView.addCodeTablePropertyWithSelectionWindow(key, value.codeTableId + '-' + value.name, restoreValue, required);
                        } else {
                            propertiesView.addCodeTablePropertyWithSelectionWindow(key, null, restoreValue, required);
                        }
                        break;
                    case 'LOADPROFILETYPE':
                        if (value !== null) {
                            propertiesView.addLoadProfileTypePropertyWithSelectionWindow(key, value.loadProfileTypeId + '-' + value.name, restoreValue, required);
                        } else {
                            propertiesView.addLoadProfileTypePropertyWithSelectionWindow(key, null, restoreValue, required);
                        }
                        break;
                    case 'REFERENCE':
                        if (selectionMode === 'COMBOBOX') {
                            properties.addComboBoxTextProperty(key, predefinedPropertyValues, value, exhaustive, restoreValue, required);
                        }
                        break;
                    case 'EAN13':
                        propertiesView.addEan13StringProperty(key, value, restoreValue, required);
                        break;
                    case 'EAN18':
                        propertiesView.addEan18StringProperty(key, value, restoreValue, required);
                        break;
                    case 'USERFILEREFERENCE':
                        if (value !== null) {
                            propertiesView.addUserReferenceFilePropertyWithSelectionWindow(key, value.userFileReferenceId + '-' + value.name, restoreValue, required);
                        } else {
                            propertiesView.addUserReferenceFilePropertyWithSelectionWindow(key, null, restoreValue, required);
                        }
                        break;
                    case 'ENCRYPTED_STRING':
                        propertiesView.addTextProperty(key, value, restoreValue, required);
                        break;
                    case 'UNKNOWN':
                        propertiesView.addTextProperty(key, value, restoreValue, required);
                        break;
                }
                if (hidden !== true) {
                    me.enableDeleteButton(key, required, isInheritedValue);
                }
            }
        )
    },

    showCodeTableOverview: function (button) {
        this.buttonClicked = button;
        this.codeTableSelectionWindow = Ext.widget('codeTableSelectionWindow');
    },

    selectCodeTable: function () {
        if (this.getCodeTableSelectionGrid().getSelectionModel().hasSelection()) {
            var codeTable = this.getCodeTableSelectionGrid().getSelectionModel().getSelection()[0];
            var view = this.getPropertyEdit();
            view.down('#' + this.buttonClicked.itemId.substr(4)).setValue(codeTable.data.codeTableId + '-' + codeTable.data.name);
        }
        this.codeTableSelectionWindow.close();
    },

    closeCodeTableSelectionWindow: function () {
        this.codeTableSelectionWindow.close();
    },

    resetValue: function (button) {
        var view = this.getPropertyEdit();
        view.down('#' + button.itemId.substr(10)).reset();
    },

    enableDeleteButton: function (key, required, isInheritedOrDefaultValue) {
        if (isInheritedOrDefaultValue === false) {
            Ext.ComponentQuery.query('#btn_delete_' + key)[0].setVisible(true);
        }
        this.enableRestoreAllButton();
    },

    enableRestoreAllButton: function () {
        var me = this;
        if (typeof(me.getRestoreAllButton()) !== 'undefined') {
            me.getRestoreAllButton().disable();
            var restoreAllButtons = Ext.ComponentQuery.query('defaultButton');
            if (restoreAllButtons != null) {
                restoreAllButtons.forEach(function (restoreButton) {
                    if (!restoreButton.isHidden()) {
                        me.getRestoreAllButton().enable();
                    }
                })
            }
        }
    },


    restoreDefaultProperty: function (button) {
        var view = this.getPropertyEdit();
        var key = button.itemId.substr(11);
        var property = this.propertiesStore.findRecord('key', key);
        var newValue = null;
        try {
            property.getPropertyValue().value = null;
            newValue = property.getPropertyValue().data.inheritedValue;
            if (typeof(newValue) === 'undefined' || newValue === null || newValue === '') {
                newValue = property.getPropertyValue().data.defaultValue;
            }

        } catch (ex) {

        }
        if (property.getPropertyType().data.simplePropertyType === 'NULLABLE_BOOLEAN') {
            if (newValue === 'false') {
                view.down('#' + key).setValue({rb: 0});
            } else if (newValue === 'true') {
                view.down('#' + key).setValue({rb: 1});
            } else {
                view.down('#' + key).setValue({rb: null});
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'CLOCK') {
            if (newValue !== null && newValue !== '' && newValue !== undefined) {
                var newDate = new Date(newValue);
                var dateValue = new Date(newDate.getFullYear(), newDate.getMonth(), newDate.getDate(), 0, 0, 0, 0);
                var timeValue = new Date(1970, 0, 1, newDate.getHours(), newDate.getMinutes(), newDate.getSeconds(), 0);
                view.down('#' + 'date' + key).setValue(dateValue);
                view.down('#' + 'time' + key).setValue(timeValue);
            } else {
                view.down('#' + 'date' + key).setValue(null);
                view.down('#' + 'time' + key).setValue(null);
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'TIMEOFDAY') {
            if (newValue !== null && newValue !== '' && newValue !== undefined) {
                view.down('#' + 'time' + key).setValue(new Date(newValue));
            } else {
                view.down('#' + 'time' + key).setValue(null);
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'DATE') {
            if (newValue !== null && newValue !== '' && newValue !== undefined) {
                view.down('#' + 'date' + key).setValue(new Date(newValue));
            } else {
                view.down('#' + 'date' + key).setValue(null);
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'CODETABLE') {
            if (newValue !== null && newValue != '' && newValue != undefined) {
                view.down('#' + key).setValue(newValue.codeTableId + '-' + newValue.name);
            } else {
                view.down('#' + key).setValue('');
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'USERFILEREFERENCE') {
            if (newValue !== null && newValue != '' && newValue != undefined) {
                view.down('#' + key).setValue(newValue.userFileReferenceId + '-' + newValue.name);
            } else {
                view.down('#' + key).setValue('');
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'LOADPROFILETYPE') {
            if (newValue !== null && newValue != '' && newValue != undefined) {
                view.down('#' + key).setValue(newValue.loadProfileTypeId + '-' + newValue.name);
            } else {
                view.down('#' + key).setValue('');
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'TIMEDURATION') {
            try {
                var selectionMode = property.getPropertyType().getPredefinedPropertyValue().data.selectionMode;
            } catch (ex) {

            }
            if (selectionMode === 'COMBOBOX') {
                if (newValue !== null && newValue !== '' && newValue !== undefined) {
                    view.down('#' + key).setValue(newValue.count + ' ' + newValue.timeUnit);
                } else {
                    view.down('#' + key).setValue(null);
                }
            } else {

                if (newValue !== null && newValue !== '' && newValue !== undefined) {
                    view.down('#' + key).setValue(newValue.count);
                    view.down('#tu_' + key).setValue(newValue.timeUnit);
                } else {
                    view.down('#' + key).setValue(null);
                    view.down('#tu_' + key).setValue(null);
                }
            }
        } else {
            view.down('#' + key).setValue(newValue);
        }
        property.data.isInheritedOrDefaultValue = true;
        this.disableDeleteButton(key);
    },

    disableDeleteButton: function (key) {
        Ext.ComponentQuery.query('#btn_delete_' + key)[0].setVisible(false);
        this.enableRestoreAllButton();
    },

    changeProperty: function (field, value, options) {
        if (this.propertiesStore != null) {
            var itemId;
            if (field.xtype === 'datefield') {
                itemId = field.itemId.substring(4);
            } else if (field.xtype === 'timefield') {
                itemId = field.itemId.substring(4);
            } else if (field.displayField === 'timeUnit') {
                itemId = field.itemId.substring(3);
            } else {
                itemId = field.itemId;
            }
            var property = this.propertiesStore.findRecord('key', itemId);
            property.data.isInheritedOrDefaultValue = false;
            //this.propertiesStore.commitChanges();
            var required = property.data.required;
            if (this.hidden !== true) {
                this.enableDeleteButton(itemId, required, false);
            }
        }
    },
    changeRadioGroupProperty: function (field, value, options) {
        if (this.propertiesStore != null) {
            var property = this.propertiesStore.findRecord('key', field.itemId.substring(5));
            property.data.isInheritedOrDefaultValue = false;
            //this.propertiesStore.commitChanges();
            var required = property.data.required;
            this.enableDeleteButton(field.itemId.substring(5), required, false);
        }
    },
    updateProperties: function () {
        var view = this.getPropertyEdit();
        var properties = this.propertiesStore;
        if (properties != null) {
            properties.each(function (property, id) {
                    var propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    var value;
                    if (view.down('#' + property.data.key) != null) {
                        var field = view.down('#' + property.data.key);
                        value = field.getValue();
                        if (property.getPropertyType().data.simplePropertyType === 'CODETABLE') {
                            if (value !== '') {
                                var valueId = value.substr(0, value.indexOf('-'));
                                value = new Object();
                                value.codeTableId = parseInt(valueId);
                            }
                        }
                        if (property.getPropertyType().data.simplePropertyType === 'USERFILEREFERENCE') {
                            if (value !== '') {
                                var valueId = value.substr(0, value.indexOf('-'));
                                value = new Object();
                                value.userFileReferenceId = parseInt(valueId);
                            }
                        }
                        if (property.getPropertyType().data.simplePropertyType === 'LOADPROFILETYPE') {
                            if (value !== '') {
                                var valueId = value.substr(0, value.indexOf('-'));
                                value = new Object();
                                value.loadProfileTypeId = parseInt(valueId);
                            }
                        }

                    }
                    if (property.getPropertyType().data.simplePropertyType === 'NULLABLE_BOOLEAN') {
                        value = view.down('#' + property.data.key).getValue().rb;
                    }
                    if (property.getPropertyType().data.simplePropertyType === 'DATE') {
                        value = view.down('#date' + property.data.key).getValue();
                        if (value !== null && value !== '') {
                            var newDate = new Date(value.getFullYear(), value.getMonth(), value.getDate(),
                                0, 0, 0, 0);
                            value = value.getTime();
                        }
                    }
                    if (property.getPropertyType().data.simplePropertyType === 'TIMEOFDAY') {
                        value = view.down('#time' + property.data.key).getValue();
                        if (value !== null && value !== '') {
                            var newDate = new Date(1970, 0, 1, value.getHours(), value.getMinutes(), value.getSeconds(), 0);
                            value = newDate.getTime() / 1000;
                        }
                    }
                    if (property.getPropertyType().data.simplePropertyType === 'CLOCK') {
                        var timeValue = view.down('#time' + property.data.key).getValue();
                        var dateValue = view.down('#date' + property.data.key).getValue();
                        if (timeValue !== null && timeValue !== '' && dateValue !== null && dateValue !== '') {
                            var newDate = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate(),
                                timeValue.getHours(), timeValue.getMinutes(), timeValue.getSeconds(), 0);
                            value = newDate.getTime();
                        }
                    }
                    if (property.getPropertyType().data.simplePropertyType === 'TIMEDURATION') {
                        var count = view.down('#' + property.data.key);
                        var unitField = view.down('#tu_' + property.data.key);
                        if (unitField === null) {
                            if (count.getValue() != null) {
                                var countValue = count.getValue().substr(0, count.getValue().indexOf(' '));
                                var timeUnitValue = count.getValue().substr(count.getValue().indexOf(' ') + 1);
                                value = new Object();
                                value.count = parseInt(countValue);
                                value.timeUnit = timeUnitValue;
                            } else {
                                value = null;
                            }
                        } else {
                            value = new Object();
                            value.count = count.getValue();
                            value.timeUnit = unitField.getValue();
                        }

                    }
                    if (property.data.isInheritedOrDefaultValue === true) {
                        property.setPropertyValue(null);
                    } else {
                        propertyValue.set('value', value);
                        property.setPropertyValue(propertyValue);
                    }
                }
            );
        }
        return properties;
    },

    updatePropertiesWithoutView: function (model) {
        var properties = model.propertiesStore.data.items;
        if (properties != null) {
            properties.forEach(function (property) {
                    if (property.data.isInheritedOrDefaultValue === true) {
                        property.setPropertyValue(null);
                    } else {
                        propertyValue.data.value = value;
                        property.setPropertyValue(propertyValue);
                    }
                }
            );
        }
        return properties;
    },

    showUserFileReferenceOverview: function (button) {
        this.buttonClicked = button;
        this.userFileReferenceSelectionWindow = Ext.widget('userFileReferenceSelectionWindow');
    },

    selectUserFileReference: function () {
        if (this.getUserFileReferenceSelectionGrid().getSelectionModel().hasSelection()) {
            var userFileReference = this.getUserFileReferenceSelectionGrid().getSelectionModel().getSelection()[0];
            var view = this.getPropertyEdit();
            view.down('#' + this.buttonClicked.itemId.substr(4)).setValue(userFileReference.data.userFileReferenceId + '-' + userFileReference.data.name);
        }
        this.userFileReferenceSelectionWindow.close();
    },

    closeUserFileReferenceSelectionWindow: function () {
        this.userFileReferenceSelectionWindow.close();
    },

    showLoadProfileTypeOverview: function (button) {
        this.buttonClicked = button;
        this.loadProfileTypeSelectionWindow = Ext.widget('loadProfileTypeSelectionWindow');
    },

    selectLoadProfileType: function () {
        if (this.getLoadProfileTypeSelectionGrid().getSelectionModel().hasSelection()) {
            var loadProfileType = this.getLoadProfileTypeSelectionGrid().getSelectionModel().getSelection()[0];
            var view = this.getPropertyEdit();
            view.down('#' + this.buttonClicked.itemId.substr(4)).setValue(loadProfileType.data.loadProfileTypeId + '-' + loadProfileType.data.name);
        }
        this.loadProfileTypeSelectionWindow.close();
    },

    closeLoadProfileTypeSelectionWindow: function () {
        this.loadProfileTypeSelectionWindow.close();
    },

    showErrors: function (errors) {
        this.getPropertiesForm().getForm().markInvalid(errors);
    },

    restoreAllDefaults: function () {
        var me = this;
        var restoreAllButtons = Ext.ComponentQuery.query('defaultButton');
        if (restoreAllButtons != null) {
            restoreAllButtons.forEach(function (restoreButton) {
                if (!restoreButton.isHidden()) {
                    me.restoreDefaultProperty(restoreButton);
                }
            })
        }
    }
})
;

