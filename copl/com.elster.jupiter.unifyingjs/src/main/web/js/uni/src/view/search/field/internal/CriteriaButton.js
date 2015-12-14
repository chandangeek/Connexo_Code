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
        this.menu.items.each(function(item, index) {
            item.setValue(value[index]);
        });
    },

    setValue: function(value) {
        if (value && !Ext.isArray(value)) {
            value = [value];
        }

        this.value = value;
        this.updateButtonText();
        this.fireEvent('change', this, value);
    },

    getFilter: function() {
        var me = this,
            value = me.getValue();

        return new Ext.util.Filter({
            property: me.dataIndex,
            value: value ? value.map(function(v){return v.getData()}) : null,
            id: me.dataIndex
        });
    },

    reset: function() {
        this.value = null;
        this.setText(this.emptyText);
        this.fireEvent('reset');
    },

    updateButtonText: function () {
        Ext.isEmpty(this.value)
            ? this.setText(this.emptyText)
            : this.setText(this.emptyText + '&nbsp;(' + this.value.length + ')');
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
                onMouseOver: Ext.emptyFn,
                enableKeyNav: false
            }, me.menuConfig)
        });

        me.addEvents(
            "change",
            "reset"
        );

        me.callParent(arguments);
    }
});