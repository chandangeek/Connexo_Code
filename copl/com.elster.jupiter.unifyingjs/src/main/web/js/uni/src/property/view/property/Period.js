Ext.define('Uni.property.view.property.Period', {
    extend: 'Uni.property.view.property.BaseCombo',
    requires: [
        'Uni.property.store.TimeUnits'
    ],

    getNormalCmp: function () {
        var me = this;

        return [
            {
                xtype: 'numberfield',
                itemId: me.key + 'numberfield',
                name: this.getName() + '.numberfield',
                width: me.width,
                required: me.required,
                readOnly: me.isReadOnly
            },
            {
                xtype: 'combobox',
                margin: '0 0 0 16',
                itemId: me.key + 'combobox',
                name: this.getName() + '.combobox',
                store: 'Uni.property.store.TimeUnits',
              //  queryMode: 'local',
                displayField: 'timeUnit',
                valueField: 'timeUnit',
                width: me.width,
                forceSelection: false,
                required: me.required,
                readOnly: me.isReadOnly,
                allowBlank: me.allowBlank
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
            var timeDurationValue = item.get('count') + " " + item.get('timeUnit');
            store.add({key: timeDurationValue, value: timeDurationValue});
        });

        var result = this.callParent(arguments);
        result.store = store;

        return result;
    },

    getField: function () {
        return this.down('numberfield');
    },

    setValue: function (value) {
        var unit = null,
            count = null,
            timeDuration = null;

        if (value != null) {
            unit = value.timeUnit;
            count = value.count;
            timeDuration = count + ' ' + unit;
        }

        if (this.isEdit) {
            if (this.isCombo()) {
                this.getComboField().setValue(timeDuration);
            } else {
                this.getField().setValue(count);
                this.getComboField().setValue(unit);
            }
        } else {
            this.callParent([timeDuration]);
        }
    },

    getValue: function (values) {
        var me = this;
        if (!me.isCombo()) {
            var result = {};
            result.count = me.getField().getValue();
            result.timeUnit = me.getComboField().getValue();

            return result;
        } else {
            return value;
        }
    }
});