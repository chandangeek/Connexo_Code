/**
 * @class Uni.grid.filtertop.Interval
 */
Ext.define('Uni.grid.filtertop.Duration', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-duration',

    requires: [
        'Uni.grid.filtertop.DateTimeSelect'
    ],

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    dataIndex: null,
    value: undefined,

    initComponent: function () {
        var me = this;


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
        this.getDateTime().setValue(date);
    },
    getParamValue: function () {
        this.getDateTime().getParamValue();
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

    getChooseIntervalButton: function () {
        return this.down('button[action=chooseDate]');
    },

    getDateTime: function () {
        return this.down('uni-grid-filtertop-datetime');
    }
});