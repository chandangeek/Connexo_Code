Ext.define('Dsh.view.widget.FilterPanel', {
    extend: 'Ext.panel.Panel',
    alias: "widget.connections-filter-panel",
    border: true,
    header: false,
    collapsible: false,
//    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            title: Uni.I18n.translate('searchItems.filter.criteria', 'MDC', 'Criteria'),
            xtype: 'filter-toolbar',
            emptyText: 'None'
        },
        {
            xtype: 'menuseparator'
        }
    ],

    removeFilterBtn: function (btn) {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer(),
            button = null;
        if (btn instanceof Ext.button.Button) {
            button = btn;
        }
        if (btn instanceof String) {
            button = filterBar.down('button[name=' + name + ']');
        }
        if (button) {
            filterBar.remove(button, true);
            if (filterBar.items.getCount() < 1) {
                me.hide()
            }
        }

    },

    clearAll: function () {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer();
        filterBar.removeAll(true);
        me.hide()
    },


    addFilterBtn: function (name, propName, value) {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer(),
            btn = filterBar.down('button[name=' + name + ']');
        if (btn) {
            btn.setText(propName + ': ' + value)
        } else {
            btn = Ext.create('Skyline.button.TagButton', {
                text: propName + ': ' + value,
                name: name,
                listeners: {
                    closeclick: me.removeFilterBtn
                }
            });
            filterBar.add(btn)
        }
    }
});
