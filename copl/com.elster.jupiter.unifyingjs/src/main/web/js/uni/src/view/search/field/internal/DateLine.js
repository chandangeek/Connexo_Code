Ext.define('Uni.view.search.field.internal.DateLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-view-search-field-date-line',
    layout: 'hbox',
    width: '440',
    defaults: {
        margin: '0 10 0 0'
    },

    getValue: function() {
        var date = this.down('#date').getValue();
        if (date) {
            date.setHours(this.down('#hours').getValue());
            date.setMinutes(this.down('#minutes').getValue());
        }

        return date;
    },

    reset: function() {
        this.down('#filter-clear').setDisabled(true);
        this.down('#date').reset();
        this.down('#hours').reset();
        this.down('#minutes').reset();
        this.fireEvent('reset');
    },

    onChange: function() {
        var date = this.down('#date').getValue(),
            value = this.getValue();

        this.down('#filter-clear').setDisabled(!value);
        this.down('#hours').setDisabled(!date);
        this.down('#minutes').setDisabled(!date);
        this.fireEvent('change', this, value);
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.rbar = {
            width: 15,
            items: {
                xtype: 'button',
                itemId: 'filter-clear',
                ui: 'plain',
                tooltip: 'Clear filter',
                iconCls: ' icon-close4',
                disabled: true,
                hidden: true,
                style: {
                    fontSize: '16px'
                },
                handler: me.reset,
                scope: me
            }
        };

        me.items = [
            {
                xtype: 'combo',
                disabled: true,
                width: 55,
                value: me.operator
            },
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
                text: 'at',
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

        me.on('render', function() {
            var button = me.down('#filter-clear');
            me.getEl().on('mouseover', function () {
                button.setVisible(true);
            });
            me.getEl().on('mouseout', function () {
                button.setVisible(false);
            });
        });
    }
});