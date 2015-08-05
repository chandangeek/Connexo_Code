Ext.define('Uni.view.search.field.DateLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-view-search-field-date-line',
    layout: 'hbox',
    width: '440',
    changeButtonText: function () {
        var me = this;
        var panel = me.up('uni-view-search-field-date-line');
        var mainButton = me.up('uni-view-search-field-date-field');
        var clearAllButton = panel.down('#filter-clear');

        if (me.getValue() === null || (me.value === 0 || me.hidden)) {
            clearAllButton.disable(true)
        } else {
            clearAllButton.enable(true);
            mainButton.setText(mainButton.defaultText + '*');
        }
    },

    rbar: {
        width: 30,
        items: [
            {
                xtype: 'button',
                itemId: 'filter-clear',
                ui: 'plain',
                tooltip: 'Clear filter',
                iconCls: ' icon-close4',
                margin: '0px 2px 5px 6px',
                disabled: true,
                hidden: true,
                style: {
                    fontSize: '16px'
                },
                handler: function () {
                    var me = this;
                    var dateField = me.up('uni-view-search-field-date-line').down('datefield');
                    var hoursField = me.up('uni-view-search-field-date-line').down('#hours');
                    var minutesField = me.up('uni-view-search-field-date-line').down('#minutes');

                    dateField.reset();
                    if (dateField.minValue) {
                        dateField.setMinValue(null);
                    }
                    if (dateField.maxValue) {
                        dateField.setMaxValue(null);
                    }
                    hoursField.reset();
                    minutesField.reset();

                    var rangeBlock = me.up('uni-view-search-field-date-range');
                    if (rangeBlock && (me.up('#smaller-value'))) {
                        rangeBlock.down('#more-value').down('datefield').setMaxValue(null);
                        rangeBlock.down('#more-value').down('datefield').setMinValue(null);
                    }
                    if (rangeBlock && (me.up('#more-value'))) {
                        rangeBlock.down('#smaller-value').down('datefield').setMaxValue(null);
                        rangeBlock.down('#smaller-value').down('datefield').setMinValue(null);
                    }

                }
            }

        ]
    },
    listeners: {
        render: function (c) {
            var button = c.down('#filter-clear');
            c.el.on('mouseover', function () {
                button.setVisible(true);
            });
            c.el.on('mouseout', function () {
                button.setVisible(false);
            });
        }

    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'combo',
                disabled: true,
                width: 55,
                margin: '1px 30px 1px 5px'
            },
            {
                xtype: 'datefield',
                margin: '1px 5px 1px 0px',
                listeners: {
                    change: me.changeButtonText
                }
            },
            {
                xtype: 'label',
                itemId: 'label',
                text: 'at',
                margin: '5px 10px 0px 20px'
            },
            {
                xtype: 'numberfield',
                itemId: 'hours',
                value: 0,
                maxValue: 23,
                minValue: 0,
                width: 55,
                margin: '1px 10px 1px 0px',
                listeners: {
                    change: me.changeButtonText
                }

            },
            {
                xtype: 'numberfield',
                itemId: 'minutes',
                value: 0,
                maxValue: 59,
                minValue: 0,
                width: 55,
                margin: '1px 5px 1px 0px',
                listeners: {
                    change: me.changeButtonText
                }

            },
            {
                itemId: 'flex',
                hidden: true,
                flex: 0.09,
            }];

        me.callParent(arguments);
        me.down('combo').setValue(this.operator);
        if (this.hideTime) {
            me.down('#label').hide();
            me.down('#hours').hide();
            me.down('#minutes').hide();
            me.down('#flex').show();
        }
    }
});