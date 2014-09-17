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
    valueStores : {
        state: 'Dsh.store.filter.CurrentState',
        latestStatus: 'Dsh.store.filter.LatestStatus',
        latestResult: 'Dsh.store.filter.LatestResult',
        comPortPool: 'Dsh.store.filter.CommPortPool',
        connectionType: 'Dsh.store.filter.ConnectionType',
        deviceType: 'Dsh.store.filter.DeviceType'
    },

    loadRecord: function (record) {
        var me = this,
            paramsCount = 0,
            data = record.getData();
        for (key in data) {
            if (!_.isEmpty(data[key])) {
                ++paramsCount;
            }
        }
        paramsCount < 1 ? me.hide() : me.show();
        me.record = record;
    },

    addFilterBtn: function (name, propName, value) {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer(),
            btn = Ext.create('Uni.view.button.TagButton', {
                text: propName + ': ' + value,
                name: name,
                value: value
            });
        btn.on('closeclick', function (btn) {
            me.record.set(btn.name, '');
            me.record.save()
        });
        filterBar.add(btn)
    }
});