/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.systeminfo.SystemInfo', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.system-info',

    content: [
        {
            itemId: 'system-info-panel',
            title: Uni.I18n.translate('general.systemInfo', 'SAM', 'System information'),
            ui: 'large',
            bbar: {
                xtype: 'dataview',
                itemId: 'system-info-dataview',
                store: 'Sam.store.SystemInfo',
                tpl: '<tpl for=".">'
                + "<p><h2>{[Uni.I18n.translate('systemInfo.serverInfo', 'SAM', 'Server information')]}:</h2></p>"
                + '<ul>'
                + '<li>{jreInfo}</li>'
                + '<li>{jvmInfo}</li>'
                + '<li>{javaHomeInfo}</li>'
                + '<li>{javaClassPathInfo}</li>'
                + '<li>{osNameInfo}</li>'
                + '<li>{osArchInfo}</li>'
                + '<li>{timeZoneInfo}</li>'
                + '<li>{numberOfProcessorsInfo}</li>'
                + '<li>{totalMemoryInfo}</li>'
                + '<li>{freeMemoryInfo}</li>'
                + '<li>{usedMemoryInfo}</li>'
                + '<li>{lastStartedTimeInfo}</li>'
                + '<li>{serverUptimeTimeInfo}</li>'
                + '<li>{dbConnectionUrlInfo}</li>'
                + '<li>{dbUserInfo}</li>'
                + '<li>{dbMaxConnectionsNumberInfo}</li>'
                + '<li>{dbMaxStatementsPerRequestInfo}</li>'
                + '</ul>'
                + "<p><h2>{[Uni.I18n.translate('systemInfo.clientInfo', 'SAM', 'Client information')]}:</h2></p>"
                + '<ul>'
                + '<li>{browserNameInfo}</li>'
                + '<li>{browserVersionInfo}</li>'
                + '<li>{browserLanguageInfo}</li>'
                + '</ul>'
                + "<p><h2>{[Uni.I18n.translate('systemInfo.environmentParameters', 'SAM', 'Environment parameters')]}:</h2></p>"
                + "{environmentParameters}"
                + "<p><h2>{[Uni.I18n.translate('systemInfo.trustStoreContent', 'SAM', 'TrustStore content')]}:</h2></p>"
                + "{trustStoreContent}"
                + '</tpl>',
                itemSelector: 'div'
            }
        }
    ]
});