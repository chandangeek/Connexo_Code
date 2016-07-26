Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    required: [
        'Imt.purpose.view.ValidationStatusForm'
    ],
    layout: 'column',
    defaults: {
        xtype: 'container',
        columnWidth: 0.5
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
                        fieldLabel: Uni.I18n.translate('general.validationSchedule', 'IMT', 'Validation schedule'),
                        name: 'dataValidationTasks',
                        itemId: 'data-validation-tasks',
                        labelAlign: 'top',
                        renderer: function (value) {
                            var record = me.getRecord(),
                                result = '';

                            if (Ext.isArray(value)) {
                                result += '<table>';
                                Ext.Array.each(value, function (item) {
                                    var url = me.router.getRoute('administration/validationtasks/validationtask').buildUrl({
                                        taskId: item.lastValidationOccurence.task.id
                                    });
                                    result += '<tr>';
                                    result += '<td>';
                                    result += '<a href="' + url + '">' + item.lastValidationOccurence.task.name + '</a>';
                                    result += '</td>';
                                    result += '<td>';
                                    result += item.lastValidationOccurence.trigger;
                                    result += '</td>';
                                    result += '</tr>';
                                });
                                result += '</table>';
                            } else if (record) {
                                result = Uni.I18n.translate('usagepoint.purpose.validation.task.noTasks', 'IMT', 'no validation tasks has been configured for "{0}" purpose yet', [record.get('name')]);
                            }

                            return result;
                        }
                    }
                ]
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this;

        me.down('#output-validation-status-form').loadValidationInfo(record.get('validationInfo'));
        me.callParent(arguments);
    }
});