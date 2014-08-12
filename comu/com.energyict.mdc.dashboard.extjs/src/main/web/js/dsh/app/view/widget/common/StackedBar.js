Ext.define('Dsh.view.widget.common.StackedBar', {
    alias: 'widget.stacked-bar',
    extend: 'Dsh.view.widget.common.Bar',

    tooltipTpl: '<table><tpl foreach="."><tr><td>{[xkey]}</td><td>{.}</td></tr></tpl></table>',
    trackTpl: [
        '<div data-qtip="{tooltip}">',
        '<tpl foreach="count">',
            '<div class="{parent.baseCls}-track {parent.baseCls}-track-stacked {[xkey]}" style="width: {.}%;"></div>',
        '</tpl>',
        '</div>'
    ],

    prepareData: function(){
        var me = this;
        var counts = _.object(_.map(me.count, function (value, key) {
            return [key, Math.round(value * 100 / me.limit)];
        }));
        return Ext.apply(me.callParent(), {
            count: counts,
            tooltip: new Ext.XTemplate(me.tooltipTpl).apply(me.count)
        });
    }
});