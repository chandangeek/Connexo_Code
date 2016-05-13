Ext.define('Uni.property.view.property.Quantity', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.model.MeasurementUnit'
    ],

    msgTarget: 'under',
    getEditCmp: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues(),
            store = Ext.create('Ext.data.Store', {
                model: 'Uni.property.model.MeasurementUnit',
                findUnit: function (data) {
                    var store = this,
                        index = store.findBy(function (record) {
                            return data.unit === record.get('unit') && data.multiplier === record.get('multiplier');
                        });

                    return store.getAt(index);
                }
            });

        store.loadData(possibleValues, false);

        return [
            {
                xtype: 'numberfield',
                itemId: me.key + 'value',
                allowExponential: false,
                margin: '0 10 0 0',
                width: me.width - 90
            },
            {
                xtype: 'combobox',
                itemId: me.key + 'unit',
                store: store,
                width: 80,
                displayField: 'displayValue',
                valueField: 'id',
                queryMode: 'local',
                forceSelection: true,
                value: possibleValues[0] ? possibleValues[0].multiplier + possibleValues[0].unit : null
            }
        ];
    },

    getField: function () {
        return this.down('numberfield');
    },

    setValue: function (value) {
        var me = this,
            combo;

        if (!me.isEdit) {
            me.down('displayfield').setValue(me.getValueAsDisplayString(value));
        } else {
            me.down('numberfield').setValue(value ? value.value : value);
            if (value && value.unit) {
                combo = me.down('combobox');
                combo.setValue(combo.getStore().findUnit(value));
            }
        }
    },

    getValue: function () {
        var me = this,
            valueObject = {},
            value = me.down('numberfield').getValue(),
            measureCombo = me.down('combobox'),
            measureComboValue = measureCombo.getValue(),
            measure = !Ext.isEmpty(measureComboValue)
                ? measureCombo.findRecordByValue(measureComboValue).getData()
                : measureComboValue;

        if (Ext.isEmpty(value)) {
            return null;
        }
        valueObject.value = value;
        valueObject.unit = measure ? measure.unit : measure;
        valueObject.multiplier = measure ? measure.multiplier : measure;
        return valueObject;
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError();

        Ext.suspendLayouts();
        me.items.each(function (item) {
            if (item.isFormField) {
                if (error) {
                    item.addCls('x-form-invalid');
                } else {
                    item.removeCls('x-form-invalid');
                }
            }
        });
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    },

    getValueAsDisplayString: function (value) {
        var me = this;

        if (Ext.isObject(value)) {
            return ('' + (value.value || '') + (value.displayValue ? ' ' + value.displayValue : '') || '-');
        } else {
            return '-';
        }
    }
});