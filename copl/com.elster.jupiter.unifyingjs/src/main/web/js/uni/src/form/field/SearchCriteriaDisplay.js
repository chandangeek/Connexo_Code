Ext.define('Uni.form.field.SearchCriteriaDisplay', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.search-criteria-display',
    readOnly: true,
    submitValue: false,
    defaultType: 'displayfield',

    initComponent: function () {
        var me = this;

        if (me.value) {
            me.items = me.getItems(me.value);
        }

        me.callParent(arguments);
    },

    setValue: function (value) {
        var me = this;

        me.value = value;
        Ext.suspendLayouts();
        me.removeAll();
        me.add(me.getItems(value));
        Ext.resumeLayouts(true);
    },

    getItems: function (value) {
        var items = [],
            groups = _.groupBy(value, function (criteria) {return criteria.group ? criteria.group.id : null});


        return items;
    }
});