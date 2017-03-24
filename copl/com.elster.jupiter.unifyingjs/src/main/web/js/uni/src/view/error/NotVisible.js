/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.error.NotVisible', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.errorNotVisible',
    itemId: 'errorNotVisible',
    overflowY: 'auto',
    requires: [
        'Ext.panel.Panel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate(
                'error.pageNotVisible',
                'UNI',
                "Sorry! We couldn't show you this."
            ),
            layout: {
                type: 'fit',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate(
                        'error.pageNotVisible',
                        'UNI',
                        'Page not visible'
                    ),
                    reasons: [
                        Uni.I18n.translate(
                            'error.pageNotVisibleAuthorized',
                            'UNI',
                            "You are not authorized to access this page."
                        ),
                        Uni.I18n.translate(
                            'error.pageNotVisibleLicenseExpired',
                            'UNI',
                            "Your license has expired."
                        )
                    ],
                    stepItems: []
                }
            ]
        }
    ]
});