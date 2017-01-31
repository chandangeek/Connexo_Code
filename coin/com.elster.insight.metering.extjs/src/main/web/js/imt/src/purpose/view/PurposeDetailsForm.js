Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    requires: [
        'Imt.purpose.view.PurposeActionsMenu',
        'Imt.purpose.view.ValidationStatusForm',
        'Imt.purpose.view.ScheduleField',
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
                        router: me.router,
                        showSuspectReasonField: false
                    }
                ]
            },
            {
                defaults: defaults,
                items: [
                    {
                        xtype: 'schedule-field',
                        itemId: 'validation-schedule',
                        fieldLabel: Uni.I18n.translate('general.validationSchedule', 'IMT', 'Validation schedule'),
                        store: 'Imt.purpose.store.ValidationTasks',
                        buttonItemId: 'view-validation-button',
                        route: me.router.getRoute('administration/validationtasks/validationtask'),
                        emptyText: Uni.I18n.translate('usagepoint.purpose.validation.task.noTasks.on.usagePoint', 'IMT', 'No validation tasks have been configured for this usage point yet ({0}manage validation tasks{1})',
                            [
                                '<a href="'
                                + me.router.getRoute('administration/validationtasks').buildUrl()
                                + '">',
                                '</a>'
                            ],
                            false)
                    },
                    {
                        xtype: 'schedule-field',
                        itemId: 'estimation-schedule',
                        fieldLabel: Uni.I18n.translate('general.estimationSchedule', 'IMT', 'Estimation schedule'),
                        store: 'Imt.purpose.store.EstimationTasks',
                        buttonItemId: 'view-estimation-button',
                        route: me.router.getRoute('administration/estimationtasks/estimationtask'),
                        emptyText: Uni.I18n.translate('usagepoint.purpose.estimation.task.noTasks.on.usagePoint', 'IMT', 'No estimation tasks has been configured for this usage point yet ({0}manage estimation tasks{1})',
                            [
                                '<a href="'
                                + me.router.getRoute('administration/estimationtasks').buildUrl()
                                + '">',
                                '</a>'
                            ],
                            false)
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