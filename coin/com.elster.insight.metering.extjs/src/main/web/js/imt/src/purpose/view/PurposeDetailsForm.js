Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    itemId: 'purpose-details-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this,
            status = me.record.get('status'),
            icon = '&nbsp;&nbsp;<i class="icon ' + (status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle2') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                + status.name
                + '"></i>';

        me.items = [
            {
                xtype: 'displayfield',
                name: 'status',
                itemId: 'purpose-status',
                fieldLabel: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                value: me.record ? (me.record.get('status').name + icon) : null,
                htmlEncode: false
            }
        ];

        me.callParent();
    }
});