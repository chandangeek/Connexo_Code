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
                tooltip += '<tr>'
                    + '<td style="text-align: right;border: none">'
                    + '<b>' + Uni.I18n.translate('lastEventType.' + key, 'UNI', key) + ':' + '</b>'
                    + '</td>'
                    + '<td>'
                    + '&nbsp;&nbsp;&nbsp;' + value.name + ' ' + '(' + value.id + ')'
                    + '</td>'
                    + '</tr>';
            }
        });

        tooltip += '</table>';

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; width: 115px; float: left;">' + result + '</span>';
    }
});