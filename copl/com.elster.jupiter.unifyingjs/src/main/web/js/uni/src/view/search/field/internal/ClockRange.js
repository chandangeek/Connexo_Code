Ext.define('Uni.view.search.field.internal.ClockRange', {
    extend: 'Uni.view.search.field.internal.DateRange',
    alias: 'widget.uni-search-internal-clockrange',
    requires: [
        'Uni.view.search.field.internal.ClockField'
    ],

    createCriteriaLine: function () {
        var me = this;

        return [
            {
                xtype: 'uni-search-internal-clock',
                itemId: 'from',
                listeners: {
                    change: function(field) {
                        me.down('#to').setMinValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#to').setMinValue(null);
                    }
                }
            },
            {
                xtype: 'uni-search-internal-clock',
                itemId: 'to',
                listeners: {
                    change: function(field) {
                        me.down('#from').setMaxValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#from').setMaxValue(null);
                    }
                }
            }
        ]
    }
});