Ext.define('Uni.view.search.field.internal.DateTimeField', {
    extend: 'Uni.form.field.DateTime',
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

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.dateConfig = {
            listeners: {
                change: me.onChange,
                scope: me
            }
        };

        me.dateTimeSeparatorConfig = {
            hidden: this.hideTime,
            html: Uni.I18n.translate('search.field.datetime.at', 'UNI', 'at'),
            margin: '0 5 0 5'
        };

        me.hoursConfig = {
            hidden: me.hideTime,
            disabled: true,
            listeners: {
                change: me.onChange,
                scope: me
            }
        };

        me.minutesConfig = {
            hidden: me.hideTime,
            disabled: true,
            listeners: {
                change: me.onChange,
                scope: me
            }
        };

        me.separatorConfig = {
            hidden: me.hideTime
        };

        me.callParent(arguments);
    },

    onChange: function () {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            value = me.getValue();

        Ext.suspendLayouts();
        me.down('#date-time-field-hours').setDisabled(!date);
        me.down('#date-time-field-minutes').setDisabled(!date);
        Ext.resumeLayouts(true);
        me.fireEvent('change', me, value);
    },

    getValue: function () {
        var me = this,
            date = me.callParent(arguments);

        return date ? date.getTime() : null;
    }
});