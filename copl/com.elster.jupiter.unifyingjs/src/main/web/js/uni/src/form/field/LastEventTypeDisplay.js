/**
 * @class Uni.form.field.LastEventTypeDisplay
 */
Ext.define('Uni.form.field.LastEventTypeDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'last-event-type-displayfield',
    name: 'lastEventType',
    fieldLabel: Uni.I18n.translate('lastEventType.label', 'UNI', 'Last event type'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (data, field) {
        var result = '',
            tooltip = '<table>';

        if (!data) {
            return this.emptyText;
        }

        Ext.Object.each(data, function(key, value) {
            if (key === 'code') {
                result = value;
            } else {
                var translation = ''
                switch(key){
                    case 'label':
                        translation = Uni.I18n.translate('lastEventType.label', 'UNI', 'Type of last event');
                        break;
                    case 'deviceType':
                        translation = Uni.I18n.translate('lastEventType.deviceType', 'UNI', 'Device type');
                        break;
                    case 'domain':
                        translation = Uni.I18n.translate('lastEventType.domain', 'UNI', 'Domain');
                        break;
                    case 'subDomain':
                        translation = Uni.I18n.translate('lastEventType.subDomain', 'UNI', 'Subdomain');
                        break;
                    case 'eventOrAction':
                        translation = Uni.I18n.translate('lastEventType.eventOrAction', 'UNI', 'Event or action');
                        break;
                }
                tooltip += '<tr>'
                    + '<td style="text-align: right;border: none">'
                    + '<b>' + translation + ':' + '</b>'
                    + '</td>'
                    + '<td>'
                    + '&nbsp;&nbsp;&nbsp;' + value.name + ' ' + '(' + value.id + ')'
                    + '</td>'
                    + '</tr>';
            }
        });

        tooltip += '</table>';
        return '<span style="display: inline-block; width: 115px; float: left;" >' + result + '</span><span style="display: inline-block; width: 16px; height: 16px;" class="uni-icon-info-small" data-qtip="' + Ext.htmlEncode(tooltip) + '"></span>';
    }
});