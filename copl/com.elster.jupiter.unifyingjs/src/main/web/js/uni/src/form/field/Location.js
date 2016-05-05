/**
 * @class Uni.form.field.Location
 */
Ext.define('Uni.form.field.Location', {
    extend: 'Ext.form.Panel',
    alias: 'widget.location',
    mixins: {
        field: 'Ext.form.field.Field'
    },
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
    defaults: {
        labelWidth: 150,
        width: 335
    },
    editLocation: Uni.I18n.translate('location.editLocation', 'UNI', 'Input location'),
    findLocationsUrl: null,
    locationDetailsUrl: null,
    initComponent: function () {
        var me = this, comboLocation, editLocation, propertyFormLocation, store;

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
            }


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
            margin: Ext.String.format('0 0 0 {0}', this.labelWidth+15),
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

        me.callParent(arguments);
        if (me.value) {
            me.setValue(me.value);
        }
    },

    onComboSelect: function (field, newValue, oldValue) {
        var me = this,
            propertyForm = me.down('#property-form-location'),
            url = Ext.String.format('{0}/{1}', me.locationDetailsUrl, newValue[0].get('id')),
            comboLocation = me.down('#combo-location'),
            editLocation = me.down('#edit-location');

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
                }
                field.locationId = newValue[0].get('id');
                comboLocation.setRawValue(recordProperties.get('unformattedLocationValue'));
            }
        })
    },

    onEditChange: function (field, newValue, oldValue) {
        var me = this,
            comboLocation = me.down('#combo-location'),
            propertyForm = me.down('#property-form-location');

        propertyForm.setVisible(newValue);
        comboLocation.setReadOnly(newValue);
        comboLocation.reset();
    },

    setValue: function (value) {
        var me = this,
            comboLocation = me.down('#combo-location'),
            propertyForm = me.down('#property-form-location'),
            editLocation = me.down('#edit-location');

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
        comboLocation.setRawValue(value.unformattedLocationValue);
        propertyForm.hide();
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

        } else {
            value.locationValue = '';
            value.locationId = comboLocation.locationId;
        }
        return value;
    },

    markInvalid: function (fields) {
        alert(fields);
    }
});