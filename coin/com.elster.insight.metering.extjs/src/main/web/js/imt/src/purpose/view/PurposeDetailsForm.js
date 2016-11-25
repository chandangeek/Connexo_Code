Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    requires: [
        'Imt.purpose.view.PurposeActionsMenu',
        'Imt.purpose.view.ValidationStatusForm',
        'Imt.purpose.view.ValidationTasksStatus',
        'Cfg.model.ValidationTask'
    ],
    itemId: 'purpose-details-form',
    layout: 'hbox',
    defaults: {
        xtype: 'container',
        flex: 1
    },

    router: null,

    initComponent: function () {
        var me = this,
            defaults = {
                xtype: 'displayfield',
                labelWidth: 200
            };

        me.items = [
            {
                defaults: defaults,
                items: [
                    {
                        name: 'status',
                        itemId: 'purpose-status',
                        fieldLabel: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                        htmlEncode: false,
                        renderer: function (status, meta, record) {
                            if (!Ext.isEmpty(status)) {
                                var icon = '&nbsp;&nbsp;<i class="icon ' + (status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
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
                        itemId: 'output-validation-status-form',
                        defaults: defaults,
                        router: me.router
                    }
                ]
            },
            {
                defaults: defaults,
                items: [
                    {
                        xtype: 'output-validation-tasks-status',
                        purpose: me.record,
                        router: me.router,
                        usagePoint: me.usagePoint
                    }

                ]
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.down('#output-validation-status-form').loadValidationInfo(record.get('validationInfo'));
        Ext.resumeLayouts(true);
        me.callParent(arguments);
    }
});