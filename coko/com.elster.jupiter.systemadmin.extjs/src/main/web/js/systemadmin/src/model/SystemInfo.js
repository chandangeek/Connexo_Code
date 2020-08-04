/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.SystemInfo', {
    extend: 'Ext.data.Model',
    fields: [

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
            name: 'environmentParameters',
            mapping: function (data) {
                output = '<ul>';
                for (propName in data.environmentParameters) {
                    output += '<li>' + propName + ': ' + data.environmentParameters[propName] + '</li>';
                }
                output += '</ul>';
                return output;
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