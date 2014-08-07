Ext.define('Dsh.view.widget.summary.Dataview',{
    extend: 'Ext.view.View',
    alias: 'widget.summary-dataview',
    itemSelector: 'tbody.item',
    total: 0,
    requires: [
        'Dsh.view.widget.Bar'
    ],

    tpl:
        '<table>' +
            '<tpl for=".">' +
            '<tbody class="item">' +
                '{% var parentIndex = xindex; %}' +
                '<tr><td class="label"><a href="#{alias}">{title}</a></td><td width="100%" id="bar-{[parentIndex]}" class="bar-{alias}"></td></tr>' +
                '<tpl for="child">' +
                    '<tr class="child"><td class="label"><a href="#{alias}">{title}</a></td><td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{alias}"></td></tr>' +
                '</tpl>' +
            '</tbody>' +
            '</tpl>' +
        '</table>'
    ,

    refresh: function () {
        var me = this;
        me.callParent(arguments);

        Ext.each(me.getNodes(), function (node, index) {
            var record = me.getRecord(node);
            var pos = index + 1;

            if (record.get('child')) {
                Ext.each(record.get('child'), function (data, di) {
                    var bar = Ext.create('Dsh.view.widget.Bar', {
                        limit: record.get('count'),
                        total: me.total,
                        count: data.count,
                        label: '(' + data.count + ')'
                    });

                    bar.render(me.getEl().down('#bar-' + pos + '-' + (di + 1)));
                });
            }

            var bar = Ext.create('Dsh.view.widget.Bar', {
                limit: me.total,
                total: me.total,
                count: record.get('count'),
                label: '(' + record.get('count') + ')'
            });
            bar.render(me.getEl().down('#bar-' + pos));
        });
    }
});