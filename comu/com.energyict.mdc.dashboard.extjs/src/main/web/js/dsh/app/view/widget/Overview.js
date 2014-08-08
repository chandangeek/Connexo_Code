Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.overview',
    itemId: 'overview',
    title: 'Overview', // TODO: localize
    ui: 'medium',
    requires: [
        'Dsh.view.widget.Bar'
    ],
    style: {
        paddingTop: 0
    },
    colspan: 4,
    layout: 'column',
    defaults: {
        columnWidth: .5,
        listeners: {
            afterrender: function (view) {
                view.store.load({
                    callback: function () {
                        Ext.each(this.getRange(), function (item) {
                            Ext.each(item.get('counters'), function (counter, idx) {
                                Ext.create('Dsh.view.widget.Bar', {
                                    limit: counter.count,
                                    total: item.get('total'),
                                    count: counter.count,
                                    label: '(' + counter.count + ')'
                                }).render(view.el.down('#bar-' + (idx + 1)));
                            });
                        });
                    }
                });
            }
        }
    },
    items: [
        {
            xtype: 'dataview',
            store: 'OverviewPerCurrentStateInfos',
            itemSelector: 'table',
            style: {
                paddingRight: '25px'
            },
            tpl: new Ext.XTemplate(
                '<div>',
                    '<tpl for=".">',
                        '<h3>{displayName}</h3>',
                        '<table style="width: 100%">',
                            '<tpl for="counters">',
                                '<tr>',
                                    '<td style="width: 30%"><a href="#">{displayName}</a></td>',
                                    '<td><div id="bar-{#}"></div></td>',
                                '</tr>',
                            '</tpl>',
                        '</table>',
                    '</tpl>',
                '</div>'
            )
        },
        {
            xtype: 'dataview',
            store: 'OverviewPerLastResultInfos',
            itemSelector: 'table',
            style: {
                paddingLeft: '25px'
            },
            tpl: new Ext.XTemplate(
                '<div>',
                    '<tpl for=".">',
                        '<h3>{displayName}</h3>',
                        '<table style="width: 100%">',
                            '<tpl for="counters">',
                                '<tr>',
                                    '<td style="width: 30%"><a href="#">{displayName}</a></td>',
                                    '<td><div id="bar-{#}"></div></td>',
                                '</tr>',
                            '</tpl>',
                        '</table>',
                    '</tpl>',
                '</div>'
            )
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});