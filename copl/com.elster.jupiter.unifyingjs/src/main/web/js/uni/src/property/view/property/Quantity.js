Ext.define('Uni.property.view.property.Quantity', {
    extend: 'Uni.property.view.property.Base',

    msgTarget: 'under',
    getEditCmp: function () {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                fields: ['id', 'displayValue'],
                data: me.getProperty().getPossibleValues()
            });

        return [
            {
                xtype: 'numberfield',
                itemId: me.key + 'value',
                name: me.getName(),
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
                value: store.getAt(0) || null
            }
        ];
    },

    getField: function () {
        return this.down('numberfield');
    },

    setValue: function (value) {
        var me = this,
            valueRegExp = /(\d*)\:\d*\:.*/,
            unitRegExp = /\d*(\:\d*\:.*)/;

        if (Ext.isObject(value) && Ext.isString(value.id)) {
            value = value.id
        }
        if (!me.isEdit) {
            me.down('displayfield').setValue(me.getValueAsDisplayString(value));
        } else if (!Ext.isEmpty(value) && Ext.isString(value)) {
            Ext.suspendLayouts();
            me.down('numberfield').setValue(value.replace(valueRegExp, '$1'));
            me.down('combobox').setValue(value.replace(unitRegExp, '0$1'));
            Ext.resumeLayouts(true);
        }
    },

    getValue: function () {
        var me = this,
            unitRegExp = /\d*(\:\d*\:.*)/,
            value = me.down('numberfield').getValue();

        return !Ext.isEmpty(value) ? me.down('combobox').getValue().replace(unitRegExp, value + '$1') : null;
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
        var me = this,
            valueRegExp = /(\d*)\:\d*\:.*/,
            unitRegExp = /\d*(\:\d*\:.*)/,
            possibleValues = me.getProperty().getPossibleValues();

        if (!Ext.isEmpty(value) && Ext.isString(value)) {
            return value.replace(valueRegExp, '$1') + ' ' + Ext.Array.findBy(possibleValues, function (possibleValue) {return possibleValue.id === value.replace(unitRegExp, '0$1')}).displayValue;
        } else {
            return '-';
        }
    }
});