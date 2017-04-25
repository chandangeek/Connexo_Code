/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.error.NotFound', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.errorNotFound',
    itemId: 'errorNotFound',
    overflowY: 'auto',
    requires: [
        'Ext.panel.Panel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate(
                'error.pageNotFoundTitle',
                'UNI',
                "Sorry! We couldn't find it."
            ),
            layout: {
                type: 'fit',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'no-items-found-panel',
                    title: '<span style="color: #eb5642">'
                    + Uni.I18n.translate(
                        'error.pageNotFound',
                        'UNI',
                        'Page not found'
                    ) + '</span>',
                    reasons: [
                        Uni.I18n.translate(
                            'error.pageNotFoundPageMisspelled',
                            'UNI',
                            "The URL is misspelled."
                        ),
                        Uni.I18n.translate(
                            'error.pageNotFoundPageNotAvailable',
                            'UNI', "The page you are looking for is not available."
                        )
                    ],
                    stepItems: []
                }
            ]
        }
    ]
});