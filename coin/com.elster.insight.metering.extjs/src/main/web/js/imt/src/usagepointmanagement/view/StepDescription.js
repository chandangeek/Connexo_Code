Ext.define('Imt.usagepointmanagement.view.StepDescription', {
    extend: 'Ext.Component',
    alias: 'widget.step-description',
    style: 'margin: -3px 0 13px 0; font-style: italic',

    initComponent: function () {
        var me = this;

        if (!Ext.isEmpty(me.text)) {
            me.html = me.text;
        }

        me.callParent(arguments);
    },

    setText: function (text) {
        var me = this;

        me.update(text);
    }
});