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

    renderer: function (value, field) {
        var me = this;

        if (Ext.isEmpty(value)) {
            return me.emptyText;
        }

        if (Ext.isFunction(me.beforeRenderer)) {
            value = me.beforeRenderer(value, field);
        }

        me.infoTooltip && Ext.defer(this.deferredRenderer, 1, this, [value, field, me.infoTooltip]);
        return '<span style="display: inline-block; float: left; margin-right: 10px;">' + value + '</span>';
    }
});