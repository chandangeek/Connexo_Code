/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.EventTypeDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'event-type-displayfield',
    name: 'eventTypeDisplay',
    emptyText: '',
    tooltip: '',

    renderer: function (value, metaData, record) {
        if (!value) return this.emptyText;

        var tooltip = Uni.I18n.translate('general.deviceType', 'UNI', 'Device type') + ': ' + record.get('deviceTypeName') + '<br>'
            + Uni.I18n.translate('general.deviceDomain', 'UNI', 'Device domain') + ': ' + record.get('deviceDomainName') + '<br>'
            + Uni.I18n.translate('general.deviceSubDomain', 'UNI', 'Device subdomain') + ': ' + record.get('deviceSubDomainName') + '<br>'
            + Uni.I18n.translate('general.deviceEventOrAction', 'UNI', 'Device event or action') + ': ' + record.get('deviceEventOrActionName') + '<br>';

        return '<span style="display:inline-block; float:left; margin-right:7px;">' + value + '</span>' +
            '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="' + tooltip + '"></span>';
    }
});