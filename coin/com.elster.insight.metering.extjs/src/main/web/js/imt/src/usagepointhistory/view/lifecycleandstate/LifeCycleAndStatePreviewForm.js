Ext.define('Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndStatePreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.life-cycle-and-state-preview-form',
    xtype: 'life-cycle-and-state-preview-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        columnWidth: 0.5
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        name: 'type_name',
                        itemId: 'fld-type',
                        fieldLabel: Uni.I18n.translate('general.type', 'IMT', 'Type')
                    },
                    {
                        name: 'fromStateName',
                        itemId: 'fld-from',
                        fieldLabel: Uni.I18n.translate('general.from', 'IMT', 'From')
                    },
                    {
                        name: 'toStateName',
                        itemId: 'fld-to',
                        fieldLabel: Uni.I18n.translate('general.to', 'IMT', 'To')
                    },
                    {
                        name: 'status_name',
                        itemId: 'fld-status',
                        fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status')
                    },
                    {
                        name: 'user_name',
                        itemId: 'fld-user',
                        fieldLabel: Uni.I18n.translate('general.user', 'IMT', 'User')
                    },
                    {
                        name: 'transitionTime',
                        itemId: 'fld-state-change-time',
                        fieldLabel: Uni.I18n.translate('usagepointtransitionexecute.wizard.transitiondate', 'IMT', 'Transition date'),
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    defaults: {
                        labelAlign: 'top'
                    }
                },
                items: [
                    {
                        itemId: 'fld-pretransitionsContainer',
                        fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.failedPretransitionChecks', 'IMT', 'Failed pre-transition checks')
                    },
                    {
                        itemId: 'fld-autoActionsContainer',
                        fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.failedAutoActions', 'IMT', 'Failed auto actions')
                    }
                ]
            }
        ];

        me.callParent();
    }
});

