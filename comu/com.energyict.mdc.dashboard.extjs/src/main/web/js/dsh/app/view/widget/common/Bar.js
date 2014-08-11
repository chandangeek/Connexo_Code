Ext.define('Dsh.view.widget.Bar', {
    extend: 'Ext.Component',
    requires: [
        'Ext.Template',
        'Ext.CompositeElement',
        'Ext.TaskManager',
        'Ext.layout.component.ProgressBar'
    ],
    alias: 'widget.bar',
    total: 100,
    limit: 100,
    count: 0,
    tpl: '<tpl for="."><div class="label">{displayName}</div><div class="bar"></div></tpl>',
    baseCls: Ext.baseCSSPrefix + 'bar',
    renderTpl: [
        '<tpl for".">',
            '<span class="{baseCls}-label">{label}</span>',
            '<div class="{baseCls}-container" style="width: 100%;">',
                '<div class="{baseCls}-fill" style="width: {fill}%;">',
                    '<div class="{baseCls}-track" style="width: {bar}%;"></div>',
                '</div>',
            '<div>',
        '</tpl>'
    ],
    initRenderData: function () {
        var me = this;
        return Ext.apply(me.callParent(), {
            fill: Math.round(me.limit * 100 / me.total),
            bar: Math.round(me.count * 100 / me.total),
            label: me.label
        });
    }
});