Ext.define('Dsh.view.widget.common.Bar', {
    alias: 'widget.bar',
    extend: 'Ext.Component',

    requires: [
        'Ext.Template',
        'Ext.CompositeElement',
        'Ext.TaskManager',
        'Ext.layout.component.ProgressBar'
    ],

    total: 100,
    limit: 100,
    count: 0,

    baseCls: Ext.baseCSSPrefix + 'bar',

    trackTpl: '<div class="{baseCls}-track" style="width: {count}%;"></div>',
    renderTpl: [
        '<tpl for=".">',
            '<span class="{baseCls}-label">{label}</span>',
            '<div class="{baseCls}-container" style="width: 100%;">',
                '<div class="{baseCls}-fill" style="width: {limit}%;">',
                    '{track}',
                '</div>',
                '<tpl if="threshold">',
                '<div class="threshold" style="left: {threshold}%;">',
                '</tpl>',
            '<div>',
        '</tpl>'
    ],

    prepareData: function(){
        var me = this;
        return {
            limit: !me.total ? 0 : Math.round(me.limit * 100 / me.total),
            label: Ext.String.htmlEncode(me.label),
            count: !me.limit ? 0 : Math.round(me.count * 100 / me.limit),
            threshold: me.threshold
        }
    },

    initRenderData: function() {
        var me = this;

        var track = new Ext.XTemplate(me.trackTpl);
        var data = Ext.apply(me.callParent(), me.prepareData());
        return Ext.apply(data, {track: track.apply(data)});
    }
});