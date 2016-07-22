Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    requires: [
        'Imt.purpose.view.PurposeActionsMenu'
    ],
    itemId: 'purpose-details-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
            itemId: 'purpose-actions-button',
            iconCls: 'x-uni-action-iconD',
            privileges: Imt.privileges.UsagePoint.canAdministrate,
            menu: {
                xtype: 'purpose-actions-menu',
                itemId: 'purpose-actions-menu'
            }
        }
    ],

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