Ext.define('Imt.purpose.view.PurposeDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.purpose-details-form',
    requires: [
        'Imt.purpose.view.PurposeActionsMenu',
        'Imt.purpose.view.ValidationStatusForm'
    ],
    itemId: 'purpose-details-form',
    layout: 'hbox',
    defaults: {
        xtype: 'container',
        flex: 1
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
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.validationSchedule', 'IMT', 'Validation schedule'),
                        itemId: 'data-validation-tasks',
                        labelAlign: 'top',
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'dataValidationTasks',
                                itemId: 'data-validation-tasks-field',
                                renderer: function (value) {
                                    var record = me.getRecord(),
                                        result = '';

                                    if (Ext.isArray(value)) {
                                        result += '<table>';
                                        Ext.Array.each(value, function (item) {
                                            var url = me.router.getRoute('administration/validationtasks/validationtask').buildUrl({
                                                taskId: item.id
                                            });
                                            result += '<tr>';
                                            result += '<td>';
                                            result += '<a href="' + url + '">' + item.name + '</a>';
                                            result += '</td>';
                                            result += '<td>';                                            
                                            result += item.trigger;                                           
                                            result += '</td>';
                                            result += '</tr>';
                                        });
                                        result += '</table>';
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'data-validation-tasks-empty-msg',
                                hidden: true
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this,
            hasValidationTasks = !Ext.isEmpty(record.get('dataValidationTasks')),
            validationTasksEmptyMsg = me.down('#data-validation-tasks-empty-msg');

        Ext.suspendLayouts();
        me.down('#output-validation-status-form').loadValidationInfo(record.get('validationInfo'));
        me.down('#data-validation-tasks-field').setVisible(hasValidationTasks);
        validationTasksEmptyMsg.setVisible(!hasValidationTasks);
        if (!hasValidationTasks) {
            validationTasksEmptyMsg.setText(Uni.I18n.translate('usagepoint.purpose.validation.task.noTasks', 'IMT', 'No validation tasks has been configured for "{0}" purpose yet ({1}manage validation tasks{2})',
                [
                    [record.get('name')],
                    '<a href="'
                    + me.router.getRoute('administration/validationtasks').buildUrl()
                    + '">',
                    '</a>'
                ],
                false));
        }
        Ext.resumeLayouts(true);
        me.callParent(arguments);
    }
});