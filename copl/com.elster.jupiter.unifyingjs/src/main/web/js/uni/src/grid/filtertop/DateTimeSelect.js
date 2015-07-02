/**
 * @class Uni.grid.filtertop.Interval
 */
Ext.define('Uni.grid.filtertop.DateTimeSelect', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-datetime-select',

    requires: [
        'Uni.grid.filtertop.DateTime'
    ],

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    dataIndex: null,
    value: undefined,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'button',
                action: 'chooseDate',
                text: me.text || Uni.I18n.translate('grid.filter.interval.label', 'UNI', 'Interval'),
                style: 'margin-right: 0 !important;',
                textAlign: 'left',
                width: 181,
                menu: [
                    {
                        xtype: 'fieldcontainer',
                        padding: '0 0 -8 0',
                        style: 'background-color: white;',
                        layout: {
                            type: 'vbox',
                            align: 'stretchmax'
                        },
                        items: [
                            {
                                xtype: 'uni-grid-filtertop-datetime',
                                dataIndex: me.dataIndex,
                                value: me.value,
                                margins: '8 8 0 8'
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 8',
                                layout: {
                                    type: 'column',
                                    align: 'stretch',
                                    pack: 'center'
                                },
                                items: [
                                    {
                                        xtype: 'label',
                                        html: '&nbsp;',
                                        width: 48
                                    },
                                    {
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'apply',
                                        text: 'Apply'
                                    },
                                    {
                                        xtype: 'button',
                                        action: 'clear',
                                        text: 'Clear'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.initActions();
    },

    initActions: function () {
        var me = this,
            applyButton = me.down('button[action=apply]'),
            clearButton = me.down('button[action=clear]');

        applyButton.on('click', me.onApplyInterval, me);
        clearButton.on('click', me.onClearInterval, me);
    },

    onApplyInterval: function () {
        var me = this;

        me.fireFilterUpdateEvent();
        me.getChooseIntervalButton().hideMenu();
    },

    setFilterValue: function (date) {
        this.getDateTime().setFilterValue(date);
    },

    getParamValue: function () {
        return this.getDateTime().getParamValue();
    },

    applyParamValue: function () {
        this.getDateTime().applyParamValue.apply(this.getDateTime(), arguments);
    },

    onClearInterval: function () {
        var me = this;

        me.getDateTime().resetValue();
        me.fireFilterUpdateEvent();
        me.getChooseIntervalButton().hideMenu();
    },

    reset: function() {
        this.getDateTime().resetValue();
    },

    getChooseIntervalButton: function () {
        return this.down('button[action=chooseDate]');
    },

    getDateTime: function () {
        return this.down('uni-grid-filtertop-datetime');
    }
});