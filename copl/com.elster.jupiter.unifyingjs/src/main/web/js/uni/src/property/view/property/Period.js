/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Period', {
    extend: 'Uni.property.view.property.BaseCombo',
    requires: [
        'Uni.property.store.TimeUnits'
    ],
    unitComboWidth: 128,
    gapWidth: 6,

    getNormalCmp: function () {
        var me = this;

        return [
            {
                xtype: 'numberfield',
                itemId: me.key + 'numberfield',
                name: me.getName(),
                width: me.width ? me.width - me.unitComboWidth - me.gapWidth : undefined,
                required: me.required,
                readOnly: me.isReadOnly,
                allowBlank: me.allowBlank,
                blankText: me.blankText,
                minValue: 0
            },
            {
                xtype: 'combobox',
                margin: '0 0 0 5',
                itemId: me.key + 'combobox',
                name: me.getName() + '.combobox',
                store: 'Uni.property.store.TimeUnits',
                displayField: 'localizedValue',
                valueField: 'timeUnit',
                width: me.unitComboWidth,
                forceSelection: false,
                editable:false,
                required: me.required,
                readOnly: me.isReadOnly,
                allowBlank: me.allowBlank,
                blankText: me.blankText
            }
        ];
    },

    getComboCmp: function () {
        var store = Ext.create('Ext.data.Store', {
            fields: [
                {name: 'key', type: 'string'},
                {name: 'value', type: 'string'}
            ]
        });

        //clear store
        store.loadData([], false);
        this.getProperty().getPossibleValues().each(function (item) {
            var timeDurationValue = item.get('count') + ' ' + item.get('timeUnit');
            store.add({key: timeDurationValue, value: timeDurationValue});
        });

        var result = this.callParent(arguments);
        result.store = store;

        return result;
    },

    getField: function () {
        return this.down('numberfield');
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (enable) {
                this.getField().enable();
                this.down('combobox').enable();
            } else {
                this.getField().disable();
                this.down('combobox').disable();
            }
        }
    },

    setValue: function (value) {
        var unit = null,
            localizedTimeUnit = null,
            count = null,
            timeDuration = null;
        if (Ext.isObject(value)) {
            localizedTimeUnit = value.localizedTimeUnit;
            unit = value.timeUnit;
            count = value.count;
            timeDuration = this.getValueAsDisplayString(value);
        }

        if (this.isEdit) {
            if (this.isCombo()) {
                this.getComboField().setValue(timeDuration);
            } else {
                this.getField().setValue(count);
                this.getComboField().setValue(unit);
                this.getComboField().setRawValue(localizedTimeUnit);
            }
        } else {
            this.callParent([timeDuration]);
        }
    },

    updateResetButton: function () {
        var me = this,
            resetButtonHidden = this.resetButtonHidden,
            button = me.getResetButton(),
            countValue,
            timeUnitValue;

        if (me.isEdit) {
            button.setVisible(!resetButtonHidden);
            if (me.getField()) { countValue = me.getField().getValue(); }
            if (me.getComboField()) { timeUnitValue = me.getComboField().getValue(); }
            if (!me.getProperty().get('isInheritedOrDefaultValue')
                && typeof countValue !== 'undefined' && countValue !== null
                && typeof timeUnitValue !== 'undefined' && timeUnitValue !== null
            ) {
                if (Ext.isEmpty(me.getProperty().get('default'))) {
                    button.setTooltip(Uni.I18n.translate('general.restoreDefaultEmptyValue', 'UNI', 'Restore to default empty value'));
                } else {
                    button.setTooltip(
                        Ext.String.format(
                            Uni.I18n.translate('general.restoreDefaultValue', this.translationKey, 'Restore to default value "{0}"'),
                            this.getValueAsDisplayString(this.getProperty().get('default'))
                        )
                    );
                }
                button.setDisabled(false);
            } else {
                button.setTooltip(null);
                button.setDisabled(true);
            }
        } else {
            button.setVisible(false);
        }

        me.fireEvent('checkRestoreAll', me);
    },

    getValue: function () {
        var me = this,
            countValue = me.getField().getValue(),
            timeUnitValue = me.getComboField().getValue();

        if (!me.isCombo()
            && typeof countValue !== 'undefined' && countValue !== null
            && typeof timeUnitValue !== 'undefined' && timeUnitValue !== null
        ) {
            var result = {};

            result.count = countValue;
            result.timeUnit = timeUnitValue;

            return result;
        }

        return null;
    },

    getValueAsDisplayString: function (value) {
        if (Ext.isObject(value)) {
            return value.count + ' ' + value.localizedTimeUnit;
        } else {
            this.callParent(arguments);
        }
    }

});