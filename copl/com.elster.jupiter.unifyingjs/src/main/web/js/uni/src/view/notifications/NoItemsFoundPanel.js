/**
 * @class Uni.view.notifications.NoItemsFoundPanel
 */
Ext.define('Uni.view.notifications.NoItemsFoundPanel', {
    extend: 'Ext.container.Container',
    xtype: 'no-items-found-panel',

    title: Uni.I18n.translate('notifications.NoItemsFoundPanel.title', 'UNI', 'No items found'),

    reasonsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.reasonsText', 'UNI', 'This could be because:'),

    reasons: [],

    stepsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.stepsText', 'UNI', 'Possible steps:'),

    stepItems: [],

    layout: {
        type: 'vbox'
    },

    items: [
        {
            xtype: 'panel',
            itemId: 'wrapper',
            cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
            ui: 'medium',
            framed: true,
            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        var wrapper = me.down('#wrapper');

        wrapper.setTitle(me.title);

        if (Ext.isArray(me.reasons) || Ext.isString(me.reasons)) {
            var formattedReasons = me.formatReasons(me.reasons);
            wrapper.add({
                xtype: 'component',
                html: formattedReasons
            });
        }

        if (!Ext.isEmpty(me.stepItems) && Ext.isArray(me.stepItems) || Ext.isObject(me.stepItems)) {
            wrapper.add({
                xtype: 'component',
                html: '<span class="steps-text">' + me.stepsText + '</span>'
            });

            wrapper.add(me.createSteps(me.stepItems));
        }
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
                hrefTarget: '_self'
            }
        });

        if (Ext.isArray(stepItems)) {
            Ext.Array.each(stepItems, function (stepItem) {
                container.add(Ext.clone(stepItem));
            });
        } else if (Ext.isString(stepItems)) {
            container.add(Ext.clone(stepItems));
        }

        return container;
    }
});