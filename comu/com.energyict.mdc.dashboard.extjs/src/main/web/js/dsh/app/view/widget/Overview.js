Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.panel.Panel',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.overview',
    itemId: 'overview',
    title: Uni.I18n.translate('overview.widget.overview.title', 'DSH', 'Overview'),
    ui: 'medium',
    style: {
        paddingTop: 0
    },
    layout: 'column',
    defaults: {
        columnWidth: .5,
        listeners: {
            afterrender: function (view) {
                view.store.load({
                    callback: function () {
                        Ext.each(this.getRange(), function (item) {
                            Ext.each(item.get('counters'), function (counter, idx) {
                                Ext.widget('bar', {
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
    commonTpl: new Ext.XTemplate(
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
    ),
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'dataview',
                store: 'OverviewPerCurrentStateInfos',
                itemSelector: 'table',
                tpl: me.commonTpl,
                style: {
                    paddingRight: '25px'
                }
            },
            {
                xtype: 'dataview',
                store: 'OverviewPerLastResultInfos',
                itemSelector: 'table',
                tpl: me.commonTpl,
                style: {
                    paddingLeft: '25px'
                }
            }
        ];
        this.callParent(arguments);
    }
});