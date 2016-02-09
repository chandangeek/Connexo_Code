Ext.define('Isu.view.overview.Section', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.overview-of-issues-section',
    store: null,
    requires: [
        'Uni.view.widget.Bar'
    ],
    title: null,

    fillSection: function (store, section) {
        var me = this;

        if (store.getRange().length) {
            var queryString = Uni.util.QueryString.getQueryStringValues(false),
                total = store.getRange()[0].get('number');

            Ext.suspendLayouts();
            me.removeAll(true);
            if (section == 'assignee') {
                var unassigned = store.findRecord('id', -1);
                if (unassigned) {
                    store.remove(unassigned);
                    store.insert(0, unassigned);
                }
            }
            Ext.Array.each(store.getRange(), function (record) {
                if (section == 'assignee') {
                    queryString[section] = record.get('id') + (record.get('id') == -1 ? ':UnexistingType' : ':USER');
                } else {
                    queryString[section] = record.get('id');
                }
                var href = me.up('overview-of-issues').router.getRoute('workspace/issues').buildUrl(null, queryString);
                record.set('href', href);
            });
            var dataview = Ext.create('Ext.view.View', {
                style: {padding: '10px'},
                itemId: section + '-dataview',
                itemSelector: 'tbody.item',
                total: total,
                store: store,
                tpl: '<table width="100%">' +
                '<tpl for=".">' +
                '<tbody class="item item-{#}">' +
                '<tr>' +
                '<td width="50%">' +
                '<div style="overflow: hidden; text-overflow: ellipsis">' +
                '<a href="{href}">{description}</a>' +
                '</div>' +
                '</td>' +
                '<td width="50%" id="bar-{#}"></td>' +
                '</tr>' +
                '</tbody>' +
                '</tpl>' +
                '</table>',
                listeners: {
                    refresh: function (view) {
                        Ext.each(view.getNodes(), function (node, index) {
                            var record = view.getRecord(node),
                                pos = index + 1;

                            var bar = Ext.widget('bar', {
                                limit: record.get('number'),
                                total: view.total,
                                count: record.get('number'),
                                label: record.get('number')
                            }).render(view.getEl().down('#bar-' + pos));
                        });
                    }
                }
            });
            me.add(dataview);
            Ext.resumeLayouts(true);
        }
    }
});
