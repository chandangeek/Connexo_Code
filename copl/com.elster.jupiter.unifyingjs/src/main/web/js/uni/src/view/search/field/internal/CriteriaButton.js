Ext.define('Uni.view.search.field.internal.CriteriaButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-search-internal-criteria',
    requires: [
        'Ext.util.Filter'
    ],
    menuConfig: null,
    value: null,

    getValue: function() {
        return this.value
    },

    populateValue: function(value) {
        throw 'this function should be overriden';
    },

    setValue: function(value) {
        this.value = Ext.isArray(value) ? value : [value];
        this.updateButtonText();
        this.fireEvent('change', this, value);
    },

    getFilter: function() {
        var me = this;

        return new Ext.util.Filter({
            property: me.dataIndex,
            value: me.getValue().map(function(v){return v.getData()}),
            id: me.dataIndex
        });
    },

    reset: function() {
        this.value = null;
        this.setText(this.emptyText);
        this.fireEvent('reset');
    },

    updateButtonText: function () {
        var count = this.value.length;
        Ext.isEmpty(this.value)
            ? this.setText(this.emptyText)
            : this.setText(this.emptyText + '&nbsp;(' + count + ')');
    },

    initComponent: function () {
        var me = this;

        me.emptyText = me.text;
        Ext.apply(me, {
            menu: Ext.apply({
                plain: true,
                bodyStyle: {
                    background: '#fff'
                },
                padding: 0,
                minWidth: 273,
                items: me.items,
                onMouseOver: Ext.emptyFn
            }, me.menuConfig)
        });

        me.addEvents(
            "change",
            "reset"
        );

        me.callParent(arguments);
    }
});