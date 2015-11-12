Ext.define('Uni.view.search.field.internal.DateRange', {
    extend: 'Ext.form.FieldSet',
    xtype: 'uni-search-internal-daterange',
    requires: [
        'Uni.view.search.field.internal.DateTimeField'
    ],
    layout: 'vbox',
    defaults: {
        margin: '0 0 5 0'
    },
    border: false,

    setValue: function(value) {
        this.items.each(function(item, index) {
            item.setValue(value[index]);
        });
    },

    getValue: function() {
        var value = [];
        this.items.each(function(item){
            if (!Ext.isEmpty(item.getValue())) {value.push(item.getValue());}
        });
        return Ext.isEmpty(value) ? null : value;
    },

    reset: function() {
        this.items.each(function(item){
            item.reset();
        });
    },

    createCriteriaLine: function () {
        var me = this;

        return [
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'from',
                listeners: {
                    change: function(field) {
                        me.down('#to datefield').setMinValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#to datefield').setMinValue(null);
                    }
                }
            },
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'to',
                listeners: {
                    change: function(field) {
                        me.down('#from datefield').setMaxValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#from datefield').setMaxValue(null);
                    }
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change"
        );

        me.items = me.createCriteriaLine();

        me.callParent(arguments);
    }
});