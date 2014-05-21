Ext.define('Skyline.form.Panel', {
    override: 'Ext.form.Panel',
    buttonAlign: 'left',

    initComponent: function() {
        var me = this;
        var width = 100;

        if (me.defaults && me.defaults.labelWidth) {
            width = me.defaults.labelWidth;
        }
        if (me.buttons) {
            me.buttons.splice(0, 0, {
                xtype: 'tbspacer',
                width: width
            })
        }

        me.callParent(arguments);
    }
});
