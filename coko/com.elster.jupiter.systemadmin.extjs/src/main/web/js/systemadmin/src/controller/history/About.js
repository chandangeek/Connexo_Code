/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.history.About', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'about',

    routeConfig: {
        about: {
            title: Uni.I18n.translate('general.about','SAM','About'),
            route: 'about',
            controller: 'Sam.controller.about.About',
            action: 'showAbout'
        }
    }
});
