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
    alias: 'widget.contentcontainer',

    requires: [
        'Uni.view.breadcrumb.Trail'
    ],

    baseCls: Uni.About.baseCssPrefix + 'content-container',
    layout: 'border',

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the content panel. Used just as if you would use the items configuration.
     */
    content: null,

    items: [
        {
            region: 'north',
            xtype: 'container',
            itemId: 'northContainer',
            cls: 'north',
            layout: 'hbox',
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    itemId: 'breadcrumbTrail'
                }
            ]
        },
        {
            region: 'west',
            xtype: 'container',
            itemId: 'westContainer',
            cls: 'west'
        },
        {
            region: 'center',
            xtype: 'container',
            cls: 'center',
            overflowY: 'auto',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: []
        }
    ],

    initComponent: function () {
        var content = this.content;

        if (content) {
            if (!(content instanceof Ext.Component)) {
                // Never modify a passed config object, that could break the expectations of the using code.
                content = Ext.clone(content);
            }

            this.items[2].items = content;
        }
        // Else use the default config already in place.

        this.callParent(arguments);
    },

    getNorthContainer: function () {
        return this.down('#northContainer');
    },

    getBreadcrumbTrail: function () {
        return this.down('#breadcrumbTrail');
    },

    getWestContainer: function () {
        return this.down('#westContainer');
    },

    getCenterContainer: function () {
        return this.down('#centerContainer');
    }

});