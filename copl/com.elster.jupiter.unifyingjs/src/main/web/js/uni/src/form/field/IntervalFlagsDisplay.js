/**
 * @class Uni.form.field.IntervalFlagsDisplay
 */
Ext.define('Uni.form.field.IntervalFlagsDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'interval-flags-displayfield',
    name: 'intervalFlags',
    fieldLabel: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    emptyText: '',

    renderer: function (value) {
        var result,
            tooltip = '',
            flags = '<span style="display:none">(';


        if (Ext.isArray(value) && value.length) {
            result = '<span style="display: inline-block; width: 25px; float: left;">' + value.length + '</span>';
            Ext.Array.each(value, function (value, index) {
                index++;
                tooltip += Uni.I18n.translate('intervalFlags.Flag', 'UNI', 'Flag') + ' ' + index + ': ' + value + '<br>';
                flags += value + '/';
            });
            flags = flags.slice(0,-1);
            flags += ')</span>';
            result = result + flags + '<span class="uni-icon-info-small" style="display: inline-block; width: 16px; height: 16px; float: left;" data-qtip="' + Ext.htmlEncode(tooltip) + '"></span>';
        }
        return result || this.emptyText;
    }
});