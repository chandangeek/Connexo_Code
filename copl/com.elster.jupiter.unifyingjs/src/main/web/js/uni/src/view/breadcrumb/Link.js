/**
 * @class Uni.view.breadcrumb.Link
 */
Ext.define('Uni.view.breadcrumb.Link', {
    extend: 'Ext.Component',
    alias: 'widget.breadcrumbLink',
    ui: 'link',

    text: '',
    href: '',

    beforeRender: function () {
        var me = this;

        me.callParent();

        // Apply the renderData to the template args
        Ext.applyIf(me.renderData, {
            text: Ext.String.htmlEncode(me.text || '&#160;'),
            href: me.href
        });
    },

    setText: function(text) {
        Ext.apply(this.renderData, {text: text});
        this.update(this.renderTpl.apply(this.renderData));
    },

    renderTpl: [
        '<tpl if="href">',
        '<a href="{href}">',
        '</tpl>',
        '{text}',
        '<tpl if="href"></a></tpl>'
    ]
});