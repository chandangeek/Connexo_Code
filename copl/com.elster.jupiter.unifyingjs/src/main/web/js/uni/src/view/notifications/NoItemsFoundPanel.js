/**
 * @class Uni.view.notifications.NoItemsFoundPanel
 *
 * The no items found panel is primarily meant to be shown as a configuration for
 * {@link Uni.view.container.PreviewContainer#emptyComponent empty components} in a
 * {@link Uni.view.container.PreviewContainer}. It can also be used independently for whichever
 * use-case needs it.
 *
 *     @example
 *     xtype: 'preview-container',
 *     grid: {
 *         xtype: 'my-favorite-grid',
 *     },
 *     emptyComponent: {
 *         xtype: 'no-items-found-panel',
 *         title: 'No favorite items found',
 *         reasons: [
 *             'No favorite items have been defined yet.',
 *             'No favorite items comply to the filter.'
 *         ],
 *         stepItems: [
 *             {
 *                 text: 'Add item',
 *                 action: 'addItem'
 *             }
 *         ]
 *     },
 *     previewComponent: {
 *         xtype: 'my-favorite-preview'
 *     }
 */
Ext.define('Uni.view.notifications.NoItemsFoundPanel', {
    extend: 'Ext.container.Container',
    xtype: 'no-items-found-panel',

    /**
     * @cfg {String}
     *
     * Title to be shown on the panel.
     */
    title: Uni.I18n.translate('notifications.NoItemsFoundPanel.title', 'UNI', 'No items found'),

    /**
     * @cfg {String}
     *
     * Text shown above the reasons.
     */
    reasonsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.reasonsText', 'UNI', 'This could be because:'),

    /**
     * @cfg {String[]/String}
     *
     * An array of reasons formatted as string. A single string value is also
     * supported. If no reasons are given, the reasons section of the panel is
     * not shown to the user.
     *
     *     @example
     *     reasons = [
     *         'No items have been defined yet.',
     *         'No items comply to the filter.'
     *     ]
     */
    reasons: [],

    /**
     * @cfg {String}
     *
     * Text shown above the step components.
     */
    stepsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.stepsText', 'UNI', 'Possible steps:'),

    /**
     * @cfg {Object[]/Object}
     *
     * Configuration objects for the items that need to be added for possible
     * steps to take if there are no items found. By default an item configuration
     * is assumed to be a button, but any component configuration is possible.
     *
     * If no steps can or should be taken, the steps section is not shown.
     *
     *     @example
     *     stepItems = [
     *         {
     *             text: 'Add item',
     *             action: 'addItem'
     *         },
     *         {
     *             text: 'Import item',
     *             action: 'importItem'
     *         }
     *     ]
     */
    stepItems: [],

    layout: {
        type: 'vbox'
    },
    items: [
        {
            ui: 'medium',
            framed: true,
            cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
            items: [
                {
                    xtype: 'container',
                    cls: Uni.About.baseCssPrefix + 'panel-no-items-found-header',
                    items: {
                        xtype: 'container',
                        itemId: 'header'
                    }
                },
                {
                    xtype: 'panel',
                    itemId: 'wrapper',
                    ui: 'medium',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);

        Ext.suspendLayouts();

        var wrapper = me.down('#wrapper');
        var header = me.down('#header');

        header.update(me.title);

        if (Ext.isArray(me.reasons) || Ext.isString(me.reasons)) {
            var formattedReasons = me.formatReasons(me.reasons);

            wrapper.add({
                xtype: 'component',
                html: formattedReasons
            });
        }

        if (!Ext.isEmpty(me.stepItems)) {
            var itemsContainer = me.createSteps(me.stepItems);

            if (!!itemsContainer) {
                wrapper.add({
                    xtype: 'component',
                    itemId: 'no-items-found-panel-steps-label',
                    html: '<span class="steps-text">' + me.stepsText + '</span>'
                });
                wrapper.add(itemsContainer);
            }
        }

        Ext.resumeLayouts();
    },

    formatReasons: function (reasons) {
        var me = this,
            result = '<span class="reasons-text">' + me.reasonsText + '</span>',
            formattedReasons = '';

        if (Ext.isArray(reasons)) {
            Ext.Array.each(reasons, function (reason) {
                formattedReasons += me.formatReason(reason);
            });
        } else if (Ext.isString(reasons)) {
            formattedReasons += me.formatReason(reasons);
        }

        return result + '<ul>' + formattedReasons + '</ul>';
    },

    formatReason: function (reason) {
        return '<li>' + reason + '</li>';
    },

    createSteps: function (stepItems) {
        var container = Ext.create('Ext.container.Container', {
            cls: 'steps',
            layout: {
                type: 'hbox'
            },
            defaults: {
                xtype: 'button',
                hrefTarget: '_self',
                margin: '0 8px 0 0'
            }
        });
        Ext.suspendLayouts();
        if (Ext.isArray(stepItems)) {
            Ext.Array.each(stepItems, function (stepItem) {
                container.add(Ext.clone(stepItem));
            });
        } else if (Ext.isString(stepItems)) {
            container.add(Ext.clone(stepItems));
        }
        Ext.resumeLayouts();
        if (container.items.length != 0) {
            return container;
        } else {
            return null;
        }
    }
});