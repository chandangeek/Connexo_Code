Ext.define('Mdc.controller.setup.DeviceCommunicationProtocol', {
    extend: 'Ext.app.Controller',

    stores: [
        'DeviceCommunicationProtocols',
        'LicensedProtocols'
    ],

    requires: [
        'Mdc.store.LicensedProtocols',
        'Mdc.store.DeviceCommunicationProtocols',
        'Mdc.store.CodeTables',
        'Mdc.model.DeviceCommunicationProtocol',
        'Mdc.model.Property',
        'Mdc.model.PossibleValue',
        'Mdc.view.setup.property.Edit'
    ],

    models: [
        'DeviceCommunicationProtocol',
        'LicensedProtocol',
        'ProtocolFamily',
        'Property'
    ],

    views: [
        'setup.devicecommunicationprotocol.List',
        'setup.devicecommunicationprotocol.Edit',
        'setup.protocolfamily.List',
        'setup.property.Edit',
        'setup.property.CodeTable'

    ],
    refs: [
        {
            ref: 'deviceCommunicationProtocolGrid',
            selector: 'viewport #devicecommunicationprotocolgrid'
        },
        {
            ref: 'deviceCommunicationProtocolEdit',
            selector: 'viewport #deviceCommunicationProtocolEdit'
        },
        {
            ref: 'propertyEdit',
            selector: 'viewport #propertyEdit'
        },
        {
            ref: 'codeTableSelectionGrid',
            selector: ' #codeTableSelectionGrid'}
    ],

    codeTableSelectionWindow: null,
    buttonClicked: null,
    propertiesStore: null,

    init: function () {
        this.control({
            'setupDeviceCommunicationProtocols': {
                itemdblclick: this.editDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=save]': {
                click: this.updateDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=cancel]': {
                click: this.cancelDeviceCommunicationProtocol
            },
            'setupDeviceCommunicationProtocols button[action=add]': {
                click: this.add
            },
            'setupDeviceCommunicationProtocols button[action=delete]': {
                click: this.delete
            },
            'deviceCommunicationProtocolEdit combobox': {
                change: this.onChangeLicensedProtocol
            },
            'propertyEdit button[action=showCodeTable]': {
                click: this.showCodeTableOverview
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
            'propertyEdit textfield': {
                change: this.changeProperty
            },
            'propertyEdit radiogroup': {
                change: this.changeRadioGroupProperty
            },
            'propertyEdit checkbox': {
                change: this.changeProperty
            }
        });
    },

    showEditView: function (id) {
        var me = this;
        var view = Ext.widget('deviceCommunicationProtocolEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationProtocol').load(id, {
            success: function (deviceCommunicationProtocol) {
                view.down('#devicecommunicationprotocolform').loadRecord(deviceCommunicationProtocol);
                me.getLicensedProtocolsStore().load({
                    params: {

                    },
                    callback: function () {
                        if (id != undefined) {

                            var devCommProtocolId = view.down('#deviceCommunicationProtocolId');
                            devCommProtocolId.hidden = false;
                            var licensedProtocol = deviceCommunicationProtocol.getLicensedProtocol();
                            view.down('#licensedProtocol').setValue(licensedProtocol.data.licensedProtocolRuleCode);
                            view.down('#protocolJavaClassName').setValue(licensedProtocol.data.protocolJavaClassName);
                            view.down('#protocolfamilygrid').reconfigure(licensedProtocol.protocolFamiliesStore);

                            me.showProperties(deviceCommunicationProtocol, view);
                        }
                        Mdc.getApplication().getMainController().showContent(view);
                    }
                });
            }
        });
    },

    showProperties: function (deviceCommunicationProtocol, view) {
        var me = this;
        var propertiesView = view.down('#propertyEdit');
        var properties = deviceCommunicationProtocol.propertyInfosStore.data.items;
        me.propertiesStore = deviceCommunicationProtocol.propertyInfosStore;

        properties.forEach(function (entry) {
                var property = entry;
                var propertyType = property.getPropertyType().data.simplePropertyType;
                var propertyValue = null;
                var value = null;
                var key = property.data.key;
                var isInheritedValue = true;
                var propertyValidationRule = null;

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

                switch (propertyType) {
                    case 'TEXT':
                        if (selectionMode === 'COMBOBOX') {
                            propertiesView.addComboBoxTextProperty(key, predefinedPropertyValues, value, exhaustive);
                        } else {
                            propertiesView.addTextProperty(key, value);
                        }
                        break;
                    case 'TEXTAREA':
                        propertiesView.addTextAreaProperty(key, value);
                        break;
                    case 'PASSWORD':
                        propertiesView.addPasswordProperty(key, value);
                        break;
                    case 'HEXSTRING':
                        propertiesView.addHexStringProperty(key, value);
                        break;
                    case 'BOOLEAN':
                        propertiesView.addBooleanProperty(key, value);
                        break;
                    case 'NULLABLE_BOOLEAN':
                        if (value === 'true') {
                            propertiesView.addNullableBooleanProperty(key, true, false, false);
                        } else if (value === 'false') {
                            propertiesView.addNullableBooleanProperty(key, false, true, false);
                        } else {
                            propertiesView.addNullableBooleanProperty(key, false, false, true);
                        }

                        break;
                    case 'NUMBER':
                        if (selectionMode === 'COMBOBOX') {
                            propertiesView.addComboBoxNumberProperty(key, predefinedPropertyValues, value, exhaustive);
                        } else {
                            var allowDecimals = true;
                            if (propertyValidationRule != null) {
                                var minValue = propertyValidationRule.data.minimumValue;
                                var maxValue = propertyValidationRule.data.maximumValue;
                                allowDecimals = propertyValidationRule.data.allowDecimals;
                            }
                            propertiesView.addNumberProperty(key, value, minValue, maxValue, allowDecimals);
                        }
                        break;
                    case 'CLOCK':
                        propertiesView.addDateTimeProperty(key, value);
                        break;
                    case 'DATE':
                        propertiesView.addDateProperty(key, value);
                        break;
                    case 'TIMEDURATION':
                        propertiesView.addTimeDurationProperty(key, value);
                        break;
                    case 'TIMEOFDAY':
                        propertiesView.addTimeProperty(key, value);
                        break;
                    case 'CODETABLE':
                        propertiesView.addCodeTablePropertyWithSelectionWindow(key, value);
                        break;
                    case 'REFERENCE':
                        if (selectionMode === 'COMBOBOX') {
                            properties.addComboBoxTextProperty(key, predefinedPropertyValues, value, exhaustive);
                        }
                    case 'UNKNOWN':
                        propertiesView.addTextProperty(key, value);
                        break;
                }
                me.enableDeleteButton(key, property.data.required, isInheritedValue);
            }
        )
    },

    onChangeLicensedProtocol: function (field, value, options) {
        var me = this;
        var view = this.getDeviceCommunicationProtocolEdit();
        if (view != undefined && field.name === 'licensedProtocol') {
            var licensedProtocol = me.getLicensedProtocolsStore().findRecord('licensedProtocolRuleCode', value);
            if (licensedProtocol != null) {
                view.down('#protocolJavaClassName').setValue(licensedProtocol.data.protocolJavaClassName);
                view.down('#protocolfamilygrid').reconfigure(licensedProtocol.protocolFamiliesStore);
            }
        }
    },

    editDeviceCommunicationProtocol: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols', record.getId());
        Ext.History.add(url);
    },
    updateDeviceCommunicationProtocol: function (button) {
        var me = this;
        var view = this.getDeviceCommunicationProtocolEdit();
        var form = view.down('#devicecommunicationprotocolform'),
            record = form.getRecord() || Ext.create(Mdc.model.DeviceCommunicationProtocol),
            values = form.getValues();
        record.set(values);
        var licensedProtocol = me.getLicensedProtocolsStore().findRecord('licensedProtocolRuleCode', values.licensedProtocol);
        record.setLicensedProtocol(licensedProtocol);
        properties = me.propertiesStore;
        if (properties != null) {
            properties.each(function (property, id) {
                if (view.down('#' + property.data.key) != null) {
                    var value = view.down('#' + property.data.key).getValue();
                    var propertyValue = Ext.create('Mdc.model.PropertyValue');
                    if (property.data.isInheritedOrDefaultValue === true) {
                        propertyValue.data.value = null;
                    } else {
                        propertyValue.data.value = value;
                    }
                    property.setPropertyValue(propertyValue);
                    delete property.data.isInheritedOrDefaultValue;
                    properties.commitChanges();
                }
            });
        }
        record.propertyInfosStore = properties;

        record.save({
            success: function (record, operation) {
                record.commit();
                me.getDeviceCommunicationProtocolsStore().reload({
                    params: {

                    },
                    callback: function () {
                        me.showDeviceCommunicationProtocolOverview();
                    }
                });
            }
        });
    },

    showDeviceCommunicationProtocolOverview: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('devicecommunicationprotocols');
        Ext.History.add(url);
    },

    cancelDeviceCommunicationProtocol: function () {
        this.showDeviceCommunicationProtocolOverview();
    },

    add: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddDeviceCommunicationProtocol();
        Ext.History.add(url);
    },

    delete: function () {
        var recordArray = this.getDeviceCommunicationProtocolGrid().getSelectionModel().getSelection();
        if (recordArray.length > 0) {
            recordArray[0].destroy();
        }
    },
    showCodeTableOverview: function (button) {
        this.buttonClicked = button;
        this.codeTableSelectionWindow = Ext.widget('codeTableSelectionWindow');
    },

    selectCodeTable: function () {
        if (this.getCodeTableSelectionGrid().getSelectionModel().hasSelection()) {
            var codeTable = this.getCodeTableSelectionGrid().getSelectionModel().getSelection()[0];
            var view = this.getDeviceCommunicationProtocolEdit();
            view.down('#' + this.buttonClicked.itemId.substr(4)).setValue(codeTable.data.codeTableId);
        }
        this.codeTableSelectionWindow.close();
    },

    closeCodeTableSelectionWindow: function () {
        this.codeTableSelectionWindow.close();
    },

    resetValue: function (button) {
        var view = this.getDeviceCommunicationProtocolEdit();
        view.down('#' + button.itemId.substr(10)).reset();
    },

    enableDeleteButton: function (key, required, isInheritedOrDefaultValue) {
        if (isInheritedOrDefaultValue === false) {
            Ext.ComponentQuery.query('#btn_delete_' + key)[0].enable();
        }
    },

    restoreDefaultProperty: function (button) {
        var view = this.getDeviceCommunicationProtocolEdit();
        var key = button.itemId.substr(11);
        var property = this.propertiesStore.findRecord('key', key);
        var newValue;
        try {
            property.getPropertyValue().value = null;
            property.data.isInheritedOrDefaultValue = true;
            this.propertiesStore.commitChanges();
            newValue = property.getPropertyValue().data.inheritedValue;
            if (typeof(newValue) === 'undefined' || newValue === null || newValue === '') {
                newValue = property.getPropertyValue().data.defaultValue;
            }
        } catch (ex) {

        }
        if (property.getPropertyType().data.simplePropertyType === 'NULLABLE_BOOLEAN') {
            if (newValue === 'false') {
                view.down('#' + 'rg' + key).setValue({rb: 2});
            } else if (newValue === 'true') {
                view.down('#' + 'rg' + key).setValue({rb: 1});
            } else {
                view.down('#' + 'rg' + key).setValue({rb: 3});
            }
        } else if (property.getPropertyType().data.simplePropertyType === 'CLOCK') {
            view.down('#' + 'date' + key).setValue(newValue);
            view.down('#' + 'time' + key).setValue(newValue);
        } else if (property.getPropertyType().data.simplePropertyType === 'TIMEOFDAY') {
            view.down('#' + 'time' + key).setValue(newValue);
        } else if (property.getPropertyType().data.simplePropertyType === 'DATE') {
            view.down('#' + 'date' + key).setValue(newValue);
        } else {
            view.down('#' + key).setValue(newValue);
        }
        this.disableDeleteButton(key);
    },

    disableDeleteButton: function (key) {
        Ext.ComponentQuery.query('#btn_delete_' + key)[0].disable();
    },

    changeProperty: function (field, value, options) {
        if (this.propertiesStore != null) {
            console.log('field');
            console.log(field);
            console.log(value);
            console.log(options);
            var itemId;
            if (field.xtype === 'datefield') {
                itemId = field.itemId.substring(4);
            } else if (field.xtype === 'timefield') {
                itemId = field.itemId.substring(4);
            } else {
                itemId = field.itemId;
            }
            var property = this.propertiesStore.findRecord('key', itemId);
            property.data.isInheritedOrDefaultValue = false;
            this.propertiesStore.commitChanges();
            var required = property.data.required;
            this.enableDeleteButton(itemId, required, false);
        }
    },
    changeRadioGroupProperty: function (field, value, options) {
        if (this.propertiesStore != null) {
            var property = this.propertiesStore.findRecord('key', field.itemId.substring(2));
            property.data.isInheritedOrDefaultValue = false;
            this.propertiesStore.commitChanges();
            var required = property.data.required;
            this.enableDeleteButton(field.itemId.substring(2), required, false);
        }
    }
})
;
