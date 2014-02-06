/**
 * @class Uni.view.container.ContentContainer
 *
 * Common content container that will have support to set breadcrumbs, and a menu. Styling
 * will also be applied automatically to anything that is in the component.
 *
 * *Note:* The breadcrumbs and menu functionality is not yet available.
 *
 * # Example usage
 *
 *     @example
 *     Ext.create('Uni.view.container.ContentContainer', {
 *         // Normal container properties.
 *     }
 */
Ext.define('Uni.view.container.ContentContainer', {
    extend: 'Ext.container.Container',
    xtype: 'contentcontainer',
    overflowY: 'auto',

    requires: [
        'Ext.layout.component.Dock'
    ],

    baseCls: 'content-wrapper',

    renderTpl: [
//        '<div style="clear: both;">Breadcrumbs</div>',
//        '<div>',
//        '<div style="position: relative; float: left;">Menu</div>',
//        '<div style="position: relative; float: left;">',
        '{%this.renderContainer(out,values);%}'
//        '</div>',
//        '</div>'
    ],

    header: Ext.create('Ext.Component', {
        html: 'Header'
    }),
    menu: Ext.create('Ext.Component', {
        html: 'Menu'
    })

    // @private
//    getDefaultContentTarget: function () {
//        return this.body;
//    },

    // @private
//    getTargetEl: function () {
//        return this.body;
//    }

});