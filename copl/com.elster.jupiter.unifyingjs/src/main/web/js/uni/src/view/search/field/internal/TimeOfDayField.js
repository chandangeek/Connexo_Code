Ext.define('Uni.view.search.field.internal.TimeOfDayField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.uni-search-internal-timeOfDayField',
    layout: 'hbox',
    requires: [
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value'
    ],
    defaults: {
        margin: '0 10 0 0'
    },
    removable: false,

    getValue: function() {
        var hoursValue = this.down('#hours').getValue(),
            minutesValue = this.down('#minutes').getValue(),
            value = null;

        if (hoursValue || minutesValue) {
            value = 0;
            if (hoursValue) {
                value += hoursValue * 3600;
            }
            if (minutesValue) {
                value += minutesValue * 60;
            }
        }

        return value;
    },

    reset: function() {
        this.down('#hours').reset();
        this.down('#minutes').reset();
        this.fireEvent('reset');
    },

    onChange: function() {
        var value = this.getValue();

        this.fireEvent('change', this, value);
    },

    onRemove: Ext.emptyFn,

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            {
                xtype: 'label',
                itemId: 'label',
                hidden: this.hideTime,
                text: Uni.I18n.translate('search.field.datetime.at', 'UNI', 'at'),
                padding: 5,
                margin: 0
            },
            {
                xtype: 'numberfield',
                itemId: 'hours',
                value: 0,
                maxValue: 23,
                minValue: 0,
                width: 55,
                hidden: this.hideTime,
                disabled: true,
                listeners: {
                    change: me.onChange,
                    scope: me
                }
            },
            {
                xtype: 'numberfield',
                itemId: 'minutes',
                value: 0,
                maxValue: 59,
                minValue: 0,
                width: 55,
                hidden: this.hideTime,
                disabled: true,
                listeners: {
                    change: me.onChange,
                    scope: me
                }

            }
        ];

        me.callParent(arguments);
    }
});