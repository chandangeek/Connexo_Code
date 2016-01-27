Ext.define('Uni.view.search.field.internal.DateTimeField', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-search-internal-datetimefield',
    layout: 'hbox',
    requires: [
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value'
    ],
    defaults: {
        margin: '0 10 0 0'
    },
    removable: false,

    setValue: function(value) {
        if (value) {
            var date = new Date(value);
            if (date) {
                this.down('#hours').setValue(date.getHours());
                this.down('#minutes').setValue(date.getMinutes());
                this.down('#date').setValue(date);
            }
        } else {
            this.reset();
        }
    },

    getValue: function() {
        var date = this.down('#date').getValue();
        if (date) {
            date.setHours(this.down('#hours').getValue());
            date.setMinutes(this.down('#minutes').getValue());
        }

        return date ? date.getTime() : null;
    },

    reset: function() {
        this.down('#date').reset();
        this.down('#hours').reset();
        this.down('#minutes').reset();
        this.fireEvent('reset');
    },

    onChange: function() {
        var date = this.down('#date').getValue(),
            value = this.getValue();

        this.down('#hours').setDisabled(!date);
        this.down('#minutes').setDisabled(!date);
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
                xtype: 'datefield',
                itemId: 'date',
                listeners: {
                    change: me.onChange,
                    scope: me
                }
            },
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