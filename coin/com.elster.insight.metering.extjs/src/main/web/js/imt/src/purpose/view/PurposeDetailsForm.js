Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    required: [
        'Imt.purpose.view.ValidationStatusForm'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                name: 'status',
                itemId: 'purpose-status',
                fieldLabel: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                //value: me.record ? (me.record.get('status').name + icon) : null,
                htmlEncode: false,
                renderer: function (status, meta, record) {
                    if (!Ext.isEmpty(status)) {
                        var icon = '&nbsp;&nbsp;<i class="icon ' + (status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle2') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                            + status.name
                            + '"></i>';
                        return status.name + icon
                    } else {
                        return '-'
                    }
                }
            },
            {
                xtype: 'output-validation-status-form',
                itemId: 'output-validation-status-form'
            }
        ];

        me.callParent();
    }
});