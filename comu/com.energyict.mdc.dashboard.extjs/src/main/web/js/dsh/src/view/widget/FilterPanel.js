Ext.define('Dsh.view.widget.FilterPanel', {
    extend: 'Ext.panel.Panel',
    alias: "widget.connections-filter-panel",
    border: true,
    header: false,
    collapsible: false,
    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'filter-toolbar',
            title: Uni.I18n.translate('', 'MDC', 'Filter'),
            emptyText: 'None'
        },
        {
            xtype: 'menuseparator'
        }
    ],

    loadRecord: function (record) {
        this.record = record;
    },

    addFilterBtn: function (name, propName, value) {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer(),
            btn = Ext.create('Uni.view.button.TagButton', {
                text: propName + ': ' + value,
                name: name,
                value: value
            })
        ;

        btn.on('closeclick', function (btn) {
            me.record.set(btn.name, '');
            me.record.save();
        });

        filterBar.add(btn);
        me.show();
    }
});