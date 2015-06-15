/**
 * @class Uni.form.field.DisplayFieldWithInfoIcon
 */
Ext.define('Uni.form.field.DisplayFieldWithInfoIcon', {
    extend: 'Ext.form.field.Display',
    xtype: 'displayfield-with-info-icon',
    emptyText: '',

    /**
     * @cfg {String} infoTooltip
     * Info icon tooltip text.
     */
    infoTooltip: null,

    /**
     * @cfg {Function} beforeRenderer
     * Should be used instead of the {@link Ext.form.field.Display.renderer} function.
     */
    beforeRenderer: null,

    renderer: function (value, field) {
        var me = this,
            icon = '';

        if (Ext.isEmpty(value)) {
            return me.emptyText;
        }

        if (Ext.isFunction(me.beforeRenderer)) {
            value = me.beforeRenderer(value, field);
        }

        if (me.infoTooltip) {
            icon  = '<span class="uni-icon-info-small" style="width: 16px; height: 16px; display: inline-block;float: none;margin-left: 10px;vertical-align: top" data-qtip="' + me.infoTooltip + '"></span>'
        }
        return Ext.String.htmlEncode(value) + icon;
    }
});