/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.container.ContentContainer
 *
 * Common content container that supports to set breadcrumbs, content in the center, and a
 * component beside the content.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * # Example usage
 *
 *     @example
 *     Ext.create('Uni.view.container.ContentContainer', {
 *         // Other container properties.
 *
 *         side: [
 *             // Placed beside the content, used as if it was a 'items' configuration.
 *         ],
 *
 *         content: [
 *             // What you would normally place in the 'items' property.
 *         ]
 *     }
 *
 * # Visual guide
 *
 * {@img view/container/ContentContainer.png Visual guide to the container component}
 *
 * # Breadcrumbs
 *
 * You can use the built-in breadcrumbs component by either fetching it via query selector with
 * the id '#breadcrumbTrail' or call the method #getBreadcrumbTrail.
 *
 * # Changing the side or content dynamically
 *
 * If your screen has already been rendered and you want to change the visible side or content
 * component you will have to refer to it as you would with any component. There are methods to request
 * each separate wrapper:
 *
 *     * North container #getNorthContainer
 *     * Center container #getCenterContainer
 *     * West container #getWestContainer
 *
 * Try to get as much done before rendering in the {#side} and {#content} properties. Otherwise future changes
 * to the content container might impact your application.
 */
Ext.define('Uni.view.container.ContentContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.contentcontainer',
    ui: 'contentcontainer',
    overflowY: 'auto',

    requires: [
    ],

    layout: {
        type: 'hbox'
    },

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the side panel. Used just as if you would use the items configuration.
     */
    side: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the content panel. Used just as if you would use the items configuration.
     */
    content: null,

    listeners: {
        afterlayout: function () {
            var me = this,
                element = this.getEl();

            if (element.isScrollable()) {
                element.setStyle('padding-right', '16px');
            } else {
                element.setStyle('padding-right', '0px');
            }

            me.suspendEvent('afterlayout');
            me.updateLayout();
            me.resumeEvent('afterlayout');

            if (this.lastScrollPosition) {
                element.scrollTo('left', this.lastScrollPosition.left);
                element.scrollTo('top', this.lastScrollPosition.top);
            }
        }
    },

    items: [
        {
            xtype: 'container',
            itemId: 'westContainer',
            overflowY: 'auto',
            cls: 'west'
        },
        {
            xtype: 'container',
            itemId: 'centerContainer',
            overflowY: 'auto',
            cls: 'center',
            flex: 1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: []
        }
    ],

    initComponent: function () {
        var side = this.side,
            content = this.content;

        content.preserveScrollOnRefresh = true;

        if (!(side instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            side = Ext.clone(side);
        }

        this.items[0].items = side;

        if (!(content instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            content = Ext.clone(content);
        }

        this.items[1].items = content;

        // Else use the default config already in place.

        this.callParent(arguments);
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getNorthContainer: function () {
        return this.down('#northContainer');
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getWestContainer: function () {
        return this.down('#westContainer');
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getCenterContainer: function () {
        return this.down('#centerContainer');
    },

    onRender: function () {
        this.callParent(arguments);
        this.mon(this.getEl(), 'scroll', this.onScroll, this);
    },


    onScroll: function (e ,t, eOpts) {
        this.lastScrollPosition = this.getEl().getScroll();
    }
});