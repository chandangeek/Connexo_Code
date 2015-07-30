/**
 * @class Uni.grid.filtertop.Interval
 */
Ext.define('Uni.grid.filtertop.Duration', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-duration',

    requires: [
        'Uni.grid.filtertop.DateTimeSelect',
        'Uni.grid.filtertop.ComboBox',
        'Uni.store.Durations'
    ],

    mixins: [
        'Uni.grid.filtertop.Base'
    ],
    layout: 'hbox',

    dataIndexFrom: 'startDate',
    dataIndexTo: 'duration',
    defaultFromDate: undefined,
    defaultDuration: undefined,
    durationStore: null,
    hideDateTtimeSelect: false,

    initComponent: function () {
        var me = this;
        me.store = me.durationStore ? Ext.isString(me.durationStore) ? Ext.create(me.durationStore) : me.durationStore : Ext.create('Uni.store.Durations');
        me.items = [
            {
                xtype: 'uni-grid-filtertop-datetime-select',
                dataIndex: me.dataIndexFrom,
                value: me.defaultFromDate,
                text: me.text,
                margin: '0 10 0 0',
                hidden: me.hideDateTtimeSelect
            },
            {
                xtype: 'uni-grid-filtertop-combobox',
                dataIndex: me.dataIndexTo,
                multiSelect: false,
                displayField: 'localizeValue',
                valueField: 'id',
                emptyText: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                store: me.store,
                value: me.defaultDuration
            }
        ];

        me.callParent(arguments);
    },

    resetValue: function () {
        var me = this;
        me.getDateTimeSelect().reset();
        me.getDurationCombo().reset();
    },

    setFilterValue: function (data) {
        var me = this;

        if (typeof data !== 'undefined') {
            var tokens = data.split('-'),
                fromDate = tokens[0],
                duration = tokens[1];

            if (fromDate && duration) {
                me.setFromDateValue(new Date(parseInt(fromDate)));
                me.setDurationValue(duration);
            }
        }
    },

    setFromDateValue: function (date) {
        this.getDateTimeSelect().setFilterValue(date);
    },

    setDurationValue: function (duration) {
        this.getDurationCombo().setFilterValue(duration);
    },

    getParamValue: function () {
        var me = this,
            fromValue = me.getFromDateValue(),
            durationValue = me.getDurationValue();

        if (Ext.isDefined(fromValue) && Ext.isDefined(durationValue)) {
            return fromValue + '-' + durationValue;
        }

        return undefined;
    },

    getFromDateValue: function () {
        return this.getDateTimeSelect().getParamValue();
    },

    getDurationValue: function () {
        return this.getDurationCombo().getParamValue();
    },

    applyParamValue: function (params, includeUndefined, flattenObjects) {
        var me = this,
            durationValue = me.getDurationValue(),
            fromValue = me.hideDateTtimeSelect ? me.createFromDate(me.getFromDateValue(), durationValue) : me.getFromDateValue(),
            toValue   = me.createToDate(fromValue, durationValue);

        if (!includeUndefined && Ext.isDefined(fromValue) && Ext.isDefined(toValue)) {
            params[me.dataIndexFrom] = fromValue;
            params[me.dataIndexTo] = toValue;
        } else if (!Ext.isDefined(fromValue) || !Ext.isDefined(toValue)) {
            params[me.dataIndexFrom] = undefined;
            params[me.dataIndexTo] = undefined;
        }
    },

    createFromDate: function (date, durationValue) {
        var me = this, duration;

        if (Ext.isDefined(date) && !Ext.isEmpty(date) && Ext.isDefined(durationValue)) {
            duration = me.getDurationCombo().getStore().getById(durationValue);
            return date - (date - moment(date).subtract(duration.get('count'), duration.get('timeUnit')).valueOf()) / 2;
        }

        return undefined;
    },

    createToDate: function (date, durationValue) {
        var me = this, duration;
        if (Ext.isDefined(date) && !Ext.isEmpty(date) && Ext.isDefined(durationValue)) {
            duration = me.getDurationCombo().getStore().getById(durationValue);
            return moment(date).add(duration.get('timeUnit'), duration.get('count')).valueOf();
        }

        return undefined;
    },

    getDateTimeSelect: function () {
        return this.down('uni-grid-filtertop-datetime-select');
    },

    getDurationCombo: function () {
        return this.down('uni-grid-filtertop-combobox');
    }
});