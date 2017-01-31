/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.SystemInfo', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'jreInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.jre', 'SAM', 'JRE version: {0}', [data.jre]);
            }
        },
        {
            name: 'jvmInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.jvm', 'SAM', 'JVM version: {0}', [data.jvm]);
            }
        },
        {
            name: 'javaHomeInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.javaHome', 'SAM', 'Java installation directory: {0}', [data.javaHome]);
            }
        },
        {
            name: 'javaClassPathInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.javaClassPath', 'SAM', 'Java class path: {0}', [data.javaClassPath]);
            }
        },
        {
            name: 'osNameInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.osName', 'SAM', 'Operating system name: {0}', [data.osName]);
            }
        },
        {
            name: 'osArchInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.osArch', 'SAM', 'Operating system architecture: {0}', [data.osArch]);
            }
        },
        {
            name: 'timeZoneInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.timeZone', 'SAM', 'Time zone: {0}', [data.timeZone]);
            }
        },
        {
            name: 'numberOfProcessorsInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.numberOfProcessors', 'SAM', 'Number of processors: {0}', [data.numberOfProcessors]);
            }
        },
        {
            name: 'totalMemoryInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.totalMemory', 'SAM', 'Total memory: {0}', [data.totalMemory]);
            }
        },
        {
            name: 'freeMemoryInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.freeMemory', 'SAM', 'Free memory: {0}', [data.freeMemory]);
            }
        },
        {
            name: 'usedMemoryInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.usedMemory', 'SAM', 'Used memory: {0}', [data.usedMemory]);
            }
        },
        {
            name: 'lastStartedTimeInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.lastStartedTime', 'SAM', 'Last started timestamp: {0}', [Uni.DateTime.formatDateTimeLong(new Date(data.lastStartedTime))]);
            }
        },
        {
            name: 'serverUptimeTimeInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.serverUptime', 'SAM', 'Server uptime: {0}', [Uni.util.String.formatDuration(data.serverUptime)]);
            }
        },
        {
            name: 'dbConnectionUrlInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.dbConnectionUrl', 'SAM', 'DB connection url: {0}', [data.dbConnectionUrl]);
            }
        },
        {
            name: 'dbUserInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.dbUser', 'SAM', 'DB user: {0}', [data.dbUser]);
            }
        },
        {
            name: 'dbMaxConnectionsNumberInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.dbMaxConnectionsNumber', 'SAM', 'DB max connections number: {0}', [data.dbMaxConnectionsNumber]);
            }
        },
        {
            name: 'dbMaxStatementsPerRequestInfo',
            mapping: function (data) {
                return Uni.I18n.translate('systemInfo.dbMaxStatementsPerRequest', 'SAM', 'DB max statements per request: {0}', [data.dbMaxStatementsPerRequest]);
            }
        },
        {
            name: 'browserNameInfo',
            defaultValue: Uni.I18n.translate('systemInfo.browserName', 'SAM', 'Browser name: {0}', [Ext.browser.name])
        },
        {
            name: 'browserVersionInfo',
            defaultValue: Uni.I18n.translate('systemInfo.browserVersion', 'SAM', 'Browser version: {0}', [Ext.browser.version.version])
        },
        {
            name: 'browserLanguageInfo',
            defaultValue: Uni.I18n.translate('systemInfo.browserLanguage', 'SAM', 'Browser language: {0}', [navigator.language || navigator.userLanguage])
        }
    ]
});