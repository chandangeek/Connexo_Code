/**
 * @class Uni.form.field.IconDisplay
 */
Ext.define('Uni.form.field.IconDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'icon-displayfield',
    iconCls: null,
    tipString: null,
    deferredRenderer: function (field, icon) {
        field.getEl().down('.x-form-display-field').appendChild(icon);
        field.updateLayout();
    },

    renderer: function (value, field) {
        var me = this,
            icon;
        if (value) {
            icon = document.createElement('span');
            icon.className = me.iconCls;
            Ext.create('Ext.tip.ToolTip', {
                target: icon,
                html: Ext.String.htmlEncode(me.tipString)
            });
            Ext.defer(this.deferredRenderer, 1, this, [field, icon]);
        }
        return '';
    }
});