Ext.define('Uni.view.search.field.internal.CriteriaButton', {
    extend: 'Ext.button.Button',
    xtype: 'search-criteria-button',
    requires: [
        'Ext.util.Filter'
    ],
    menuConfig: null,
    value: null,

    getValue: function() {
        return this.value
    },

    getFilter: function() {
        var me = this;
        return new Ext.util.Filter({
            property: me.dataIndex,
            value: me.getValue()
        });
    },

    onChange: function(elm, value) {
        this.value = value;
        this.updateButtonText();
        this.fireEvent('change', this, value);
    },

    reset: function() {
        this.value = null;
        this.setText(this.emptyText);
        this.fireEvent('reset');
    },

    updateButtonText: function () {
        Ext.isEmpty(this.value)
            ? this.setText(this.emptyText)
            : this.setText(this.emptyText + '*');
    },

    initComponent: function () {
        var me = this;

        Ext.apply(me, {
            menu: Ext.apply({
                plain: true,
                bodyStyle: {
                    background: '#fff'
                },
                padding: 0,
                minWidth: 273,
                items: me.items
            }, me.menuConfig)
        });

        me.addEvents(
            "change",
            "reset"
        );

        me.callParent(arguments);
    }
});