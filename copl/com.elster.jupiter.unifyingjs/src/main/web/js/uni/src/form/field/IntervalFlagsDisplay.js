/**
 * @class Uni.form.field.IntervalFlagsDisplay
 */
Ext.define('Uni.form.field.IntervalFlagsDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'interval-flags-displayfield',
    name: 'intervalFlags',
    fieldLabel: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    emptyText: '',

    deferredRenderer: function (field, icon) {
        field.getEl().down('.x-form-display-field').appendChild(icon);
        field.updateLayout();
    },

    renderer: function (value, field) {
        var icon,
            tooltip = '';
        if (!Ext.isArray(value) || !value.length) {
            return this.emptyText;
        }


        icon = document.createElement('span');
        icon.className = 'icon-info-small';
        icon.setAttribute('style', 'width: 16px; height: 16px');
        Ext.Array.each(value, function (value, index) {
            index++;
            tooltip += Uni.I18n.translate('intervalFlags.Flag', 'UNI', 'Flag') + ' ' + index + ': ' + value + '<br>';
        });
        Ext.create('Ext.tip.ToolTip', {
            target: icon,
            html: tooltip
        });
        Ext.defer(this.deferredRenderer, 1, this, [field, icon]);

        return '<span style="display: inline-block; width: 20px; float: left;">' + value.length + '</span>';
    }
});