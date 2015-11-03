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
                tpl: '<tpl for=".">' +
                "<p><b>{[Uni.I18n.translate('systemInfo.serverInfo', 'SAM', 'Server information')]}:</b></p>"
                + '<p>{jreInfo}</p>'
                + '<p>{jvmInfo}</p>'
                + '<p>{javaHomeInfo}</p>'
                + '<p>{javaClassPathInfo}</p>'
                + '<p>{osNameInfo}</p>'
                + '<p>{osArchInfo}</p>'
                + '<p>{timeZoneInfo}</p>'
                + '<p>{numberOfProcessorsInfo}</p>'
                + '<p>{totalMemoryInfo}</p>'
                + '<p>{freeMemoryInfo}</p>'
                + '<p>{usedMemoryInfo}</p>'
                + '<p>{lastStartedTimeInfo}</p>'
                + '<p>{serverUptimeTimeInfo}</p>'
                + '<p>{dbConnectionUrlInfo}</p>'
                + '<p>{dbUserInfo}</p>'
                + '<p>{dbMaxConnectionsNumberInfo}</p>'
                + '<p>{dbMaxStatementsPerRequestInfo}</p>'
                + '<br>'
                + "<p><b>{[Uni.I18n.translate('systemInfo.clientInfo', 'SAM', 'Client information')]}:</b></p>"
                + '<p>{browserNameInfo}</p>'
                + '<p>{browserVersionInfo}</p>'
                + '<p>{browserLanguageInfo}</p>'
                + '</tpl>',
                itemSelector: 'div'
            }
        }
    ]
});