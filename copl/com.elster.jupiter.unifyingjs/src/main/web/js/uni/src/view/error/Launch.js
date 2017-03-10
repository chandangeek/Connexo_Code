/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.error.Launch', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.errorLaunch',
    itemId: 'errorLaunch',
    overflowY: 'auto',
    requires: [
        'Ext.panel.Panel',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate(
                'error.errorLaunchTitle',
                'UNI',
                "Sorry! An error occurred."
            ),
            layout: {
                type: 'fit',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('error.errorLaunch', 'UNI', 'Application error'),
                    reasons: [
                        Uni.I18n.translate(
                            'error.errorLaunchAuthentication',
                            'UNI',
                            "You are not authorized to access this application."
                        ),
                        Uni.I18n.translate(
                            'error.errorServerUnreachable',
                            'UNI',"Server hosting this application is unreachable."
                        )
                    ],
                    stepItems: []
                }
            ]
        }
    ]
});