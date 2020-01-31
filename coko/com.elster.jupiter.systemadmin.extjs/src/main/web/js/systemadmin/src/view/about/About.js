/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.about.About', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.about-info',
    requires: [
        'Sam.view.about.Dynamic',
        'Sam.view.about.Static'
    ],

    content: [
        {
            title: Uni.I18n.translate('general.about', 'SAM', 'About'),
            ui: 'large',
            bbar: {
                xtype: 'panel',
                title: Uni.I18n.translate('general.connexo.version', 'SAM', 'Connexo version') +  ' ' +'{connexo.version}',
                ui: 'medium',
                padding: 0,
                items: [
                    {
                        xtype: 'about-dynamic-info',
                        itemId: 'about-dynamic-part-info'
                    },
                    {
                        xtype: 'about-static-info',
                        itemId: 'about-static-part-info'
                    }
                ]
            }
        }
    ]
});