/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.Location
 */
Ext.define('Uni.form.field.Location', {
    extend: 'Ext.form.Panel',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.location',
    requires: [
        'Uni.store.FindLocations',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.model.LocationInfo'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    labelWidth: 150,

    editLocation: Uni.I18n.translate('location.editLocation', 'UNI', 'Input location'),
    findLocationsUrl: null,
    locationDetailsUrl: null,
    displayResetButton: false,
    isValid: function () {
        return true;
    },
    initComponent: function () {
        var me = this, comboLocation, locationContainer, editLocation, propertyFormLocation,
            labelPad = 15,
            store = Ext.create('Uni.store.FindLocations');

        store.getProxy().setUrl(me.findLocationsUrl);
        comboLocation = {
            xtype: 'combobox',
            itemId: 'combo-location',
            emptyText: Uni.I18n.translate('location.typingAddress', 'UNI', 'Start typing an address ...'),
            fieldLabel: Uni.I18n.translate('location.location', 'UNI', 'Location'),
            store: store,
            minChars: 1,
            displayField: 'displayValue',
            valueField: 'locationId',
            forceSelection: false,
            typeAhead: true,
            mode: 'remote',
            selectOnFocus: true,
            triggerAction: 'all',
            queryParam: 'filter',
            listeners: {
                beforequery: function (record) {
                    record.query = Ext.encode([
                        {
                            property: 'displayValue',
                            value: record.combo.getValue()
                        }]);
                },
                select: {
                    fn: me.onComboSelect,
                    scope: me
                }
            },
            style: {
                display: 'inline'
            },
            labelWidth: me.labelWidth
        };
        editLocation = {
            xtype: 'checkbox',
            boxLabel: me.editLocation,
            itemId: 'edit-location',
            listeners: {
                change: {
                    fn: me.onEditChange,
                    scope: me
                }
            },
            margin: Ext.String.format('0 0 6 {0}', this.labelWidth + 15)
        };
        propertyFormLocation = {
            xtype: 'property-form',
            itemId: 'property-form-location',
            defaults: {
                resetButtonHidden: true,
                labelWidth: this.labelWidth,
                width: '100%'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        };
        me.items = [comboLocation, editLocation, propertyFormLocation];
        me.rbar = {
            xtype: 'container',
            items: [
                {
                    xtype: 'uni-default-button',
                    itemId: 'uni-location-default-button',
                    listeners: {
                        click: {
                            fn: me.onClickDefault,
                            scope: me
                        }
                    }
                }
            ]
        };
        me.callParent(arguments);
        if (me.value) {
            me.setValue(me.value);
        }
        me.updateResetButton();
    },

    updateResetButton: function () {
        var me = this,
            defaultButton = me.down('#uni-location-default-button');

        if (me.displayResetButton) {
            defaultButton.setVisible(true);
            defaultButton.setTooltip(Uni.I18n.translate('location.locationResetTooltip', 'UNI', 'Reset to usage point location'));
        }
    },

    onComboSelect: function (field, newValue, oldValue) {
        var me = this,
            propertyForm = me.down('#property-form-location'),
            url = Ext.String.format('{0}/{1}', me.locationDetailsUrl, newValue[0].get('id')),
            comboLocation = me.down('#combo-location'),
            defaultButton = me.down('#uni-location-default-button');

        Ext.Ajax.request({
            url: url,
            method: 'GET',
            success: function (response) {
                var model = new Uni.model.LocationInfo();
                var reader = model.getProxy().getReader();
                var resultSet = reader.readRecords(Ext.decode(response.responseText));
                var recordProperties = resultSet.records[0];

                if (recordProperties && recordProperties.properties() && recordProperties.properties().count()) {
                    propertyForm.loadRecord(recordProperties);
                    propertyForm.show();
                }
                field.locationId = newValue[0].get('id');
                //comboLocation.setRawValue(recordProperties.get('unformattedLocationValue'));
                propertyForm.hide();
                defaultButton.setDisabled((me.displayValue.usagePointLocationId == undefined) ||
                    me.displayValue.usagePointLocationId == newValue[0].get('id'));
            }
        })
    },

    onEditChange: function (field, newValue, oldValue) {
        var me = this,
            comboLocation = me.down('#combo-location'),
            propertyForm = me.down('#property-form-location'),
            defaultButton = me.down('#uni-location-default-button');

        if ((newValue == true) && ((comboLocation.getRawValue() == null) ||
            ((comboLocation.getRawValue() != null) && (comboLocation.getRawValue().length == 0)))) {
            var url = Ext.String.format('{0}/{1}', me.locationDetailsUrl, 0);
            Ext.Ajax.request({
                url: url,
                method: 'GET',
                success: function (response) {
                    var model = new Uni.model.LocationInfo();
                    var reader = model.getProxy().getReader();
                    var resultSet = reader.readRecords(Ext.decode(response.responseText));
                    var recordProperties = resultSet.records[0];

                    if (recordProperties && recordProperties.properties() && recordProperties.properties().count()) {
                        propertyForm.loadRecord(recordProperties);
                        propertyForm.show();
                    }

                    propertyForm.setVisible(newValue);
                    comboLocation.setDisabled(newValue);
                    comboLocation.reset();
                }
            })
        }
        else if ((newValue == true) && (comboLocation.getRawValue() != null)) {
            comboLocation.oldRawValue = comboLocation.getRawValue();
            propertyForm.setVisible(newValue);
            comboLocation.setDisabled(newValue);
            defaultButton.setDisabled(true);
            comboLocation.reset();
        }
        else {
            propertyForm.setVisible(newValue);
            comboLocation.setDisabled(newValue);
            comboLocation.setRawValue(comboLocation.oldRawValue);
            defaultButton.setDisabled((me.displayValue.usagePointLocationId == undefined) ||
                ((me.displayValue.usagePointLocationId != undefined) && (me.displayValue.usagePointLocationId == comboLocation.locationId)));
        }
    },

    setValue: function (value) {
        var me = this,
            comboLocation = me.down('#combo-location'),
            propertyForm = me.down('#property-form-location'),
            editLocation = me.down('#edit-location'),
            defaultButton = me.down('#uni-location-default-button');


        var model = new Uni.model.LocationInfo();
        var reader = model.getProxy().getReader();
        var resultSet = reader.readRecords(value);
        var recordProperties = resultSet.records[0];
        if (recordProperties && recordProperties.properties() && recordProperties.properties().count()) {
            propertyForm.loadRecord(recordProperties);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
        comboLocation.locationId = value.locationId;
        comboLocation.setValue(value.locationValue);
        propertyForm.hide();
        editLocation.setValue(false);
        defaultButton.setDisabled((value.usagePointLocationId == undefined) ||
            ((value.usagePointLocationId != undefined) && value.isInherited));
        me.displayValue = value;
    },

    getValue: function () {
        var me = this,
            comboLocation = me.down('#combo-location'),
            editLocation = me.down('#edit-location'),
            propertyForm = me.down('#property-form-location'),
            value = {};

        var isChecked = editLocation.getValue();
        if (isChecked) {
            value.locationValue = '';
            value.locationId = -1;
            value.properties = [];
            propertyForm.updateRecord();

            propertyForm.getRecord().properties().each(function (record) {
                    var data = {};
                    data.key = record.get('key');
                    data.name = record.get('name');
                    data.required = record.get('required');
                    data.propertyValueInfo = record.getPropertyValue().data;
                    data.propertyTypeInfo = record.getPropertyType().data;
                    value.properties.push(data);
                }
            );

            if (me.displayResetButton) {
                value.isInherited = false;
            }

        } else {
            value.locationValue = '';
            value.locationId = comboLocation.locationId;

            if (me.displayResetButton) {
                value.isInherited = (comboLocation.locationId == me.displayValue.usagePointLocationId);
                value.usagePointLocationId = me.displayValue.usagePointLocationId;
            }
        }
        return value;
    },

    onClickDefault: function (button) {
        var me = this,
            propertyForm = me.down('#property-form-location'),
            url = Ext.String.format('{0}/{1}', me.locationDetailsUrl, me.displayValue.usagePointLocationId),
            comboLocation = me.down('#combo-location'),
            editLocation = me.down('#edit-location'),
            defaultButton = me.down('#uni-location-default-button');

        Ext.Ajax.request({
            url: url,
            method: 'GET',
            success: function (response) {
                var model = new Uni.model.LocationInfo();
                var reader = model.getProxy().getReader();
                var resultSet = reader.readRecords(Ext.decode(response.responseText));
                var recordProperties = resultSet.records[0];

                if (recordProperties && recordProperties.properties() && recordProperties.properties().count()) {
                    propertyForm.loadRecord(recordProperties);
                    propertyForm.show();
                }
                comboLocation.locationId = me.displayValue.usagePointLocationId;
                comboLocation.setValue(recordProperties.get('locationValue'));
                propertyForm.hide();
                defaultButton.setDisabled(me.displayValue.usagePointLocationId);
            }
        })

    }
});