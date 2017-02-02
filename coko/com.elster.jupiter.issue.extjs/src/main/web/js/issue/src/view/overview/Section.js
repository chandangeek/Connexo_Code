Ext.define('Isu.view.overview.Section', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.overview-of-issues-section',
    store: null,
    requires: [
        'Uni.view.widget.Bar'
    ],
    title: null,
    minHeight: 235,
    itemsInCollapsedMode: 5,
    buttonAlign: 'left',
    parentItemId: 'overview-of-issues',
    route: 'workspace/issues',
    buttons: [
        {
            text: Uni.I18n.translate('overview.issues.showMore', 'ISU', 'Show more'),
            hidden: true,
            margin: '0 0 0 10',
            handler: function () {
                this.up('panel').moreLess();
            }
        }
    ],

    fillSection: function (store, section) {
        var me = this;

        if (store.getRange().length) {
            var queryString = Uni.util.QueryString.getQueryStringValues(false),
                total = store.getRange()[0].get('number');

            Ext.suspendLayouts();
            me.removeAll(true);
            if (me.down('button')) {
                me.down('button').setVisible(store.getCount() > me.itemsInCollapsedMode);
                if (me.down('button').isVisible()) {
                    me.down('button').setText(Uni.I18n.translate('overview.issues.showMore', 'ISU', 'Show more'));
                }
            }
            if (section == 'userAssignee') {
                var unassigned = store.findRecord('id', -1);
                if (unassigned) {
                    store.remove(unassigned);
                    store.insert(0, unassigned);
                }
            }
            Ext.Array.each(store.getRange(), function (record) {

                queryString[section] = record.get('id');
                queryString.groupingType = 'none';
                queryString.sort = ['-priority'];
                if (me.up(me.parentItemId)) {
                    var href = me.up(me.parentItemId).router.getRoute(me.route).buildUrl(null, queryString);
                    record.set('href', href);
                }
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
                        view.collapsed = store.getCount() > me.itemsInCollapsedMode;
                        view.expandedHeight = view.getHeight();
                        view.collapsedHeight = view.expandedHeight / store.getCount() * me.itemsInCollapsedMode;
                        if (view.collapsed) view.setHeight(view.collapsedHeight - 3);
                    }
                }
            });
            me.add(dataview);
            Ext.resumeLayouts(true);
        }
    },
    moreLess: function () {
        var view = this.down('dataview');
        Ext.suspendLayouts();
        this.down('button').setText(view.collapsed ?
            Uni.I18n.translate('overview.issues.showLess', 'ISU', 'Show less') :
            Uni.I18n.translate('overview.issues.showMore', 'ISU', 'Show more'));
        view.animate({
            duration: 300,
            to: {
                height: (view.collapsed ? view.expandedHeight : view.collapsedHeight - 3)
            }
        });
        view.collapsed = !view.collapsed;
        Ext.resumeLayouts(true);
    }
});
