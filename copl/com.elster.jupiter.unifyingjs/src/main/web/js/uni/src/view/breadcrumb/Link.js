/**
 * @class Uni.view.breadcrumb.Link
 */
Ext.define('Uni.view.breadcrumb.Link', {
    extend: 'Ext.Component',
    alias: 'widget.breadcrumbLink',

    text: '',
    href: '',

    beforeRender: function () {
        var me = this;

        me.callParent();

        // Apply the renderData to the template args
        Ext.applyIf(me.renderData, {
            text: me.text || '&#160;',
            href: me.href
        });
    },

    renderTpl: [
        '<tpl if="href">',
        '<a href="{href}">',
        '</tpl>',
        '{text}',
        '<tpl if="href"></a></tpl>'
    ]
});