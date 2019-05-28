/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.ManuallyRuleItem', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.privileges.Issue'
    ],
    alias: 'widget.issue-manually-creation-rules-item',
    itemId: 'issue-manually-creation-rules-item',
    title: Uni.I18n.translate('workspace.newManuallyIssue', 'ISU', 'Create issue'),
    defaults: {
        labelWidth: 260,
        width: 600,
        validateOnChange: false,
        validateOnBlur: false,
        msgTarget: 'under'
    },
    newReasonId: '12222e48-9afb-4c76-a41e-d3c40f16ac76',
    deviceId: null,
    initComponent: function () {
        var me = this;
        var defaultUrgency = 25;
        var defaultImpact = 5;

        me.items = [{
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    name: 'form-errors',
                    margin: '0 0 10 0',
                    hidden: true
               },
               {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.title.isudevice', 'ISU', 'Device'),
                    store: 'Isu.store.IssueDevices',
                    forceSelection: true,
                    required: !me.bulkAction,
                    allowBlank: false,
                    displayField: 'name',
                    valueField: 'id',
                    name: 'deviceId',
                    queryMode: 'local',
                    itemId: 'deviceId',
                    hidden: me.bulkAction
               },
               {
                    itemId: 'issueReason',
                    xtype: 'combobox',
                    name: 'reasonId',
                    fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ISU', 'Issue reason'),
                    required: true,
                    store: 'Isu.store.IssueReasons',
                    queryMode: 'local',
                    displayField: 'name',
                    validateOnBlur: false,
                    validateOnChange: false,
                    valueField: 'id'
               },
               {
                  xtype: 'fieldcontainer',
                  fieldLabel: Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                  margin: '0 0 20 0',
                  layout: 'hbox',
                  items: [
                     {
                        xtype: 'label',
                        itemId: 'priority-label',
                        text: Uni.I18n.translate('issue.priority.low', 'ISU', 'low') + '(' + defaultUrgency + defaultImpact + ')'
                     }
                  ]
               },
               {
                  xtype: 'fieldcontainer',

                  fieldLabel: Uni.I18n.translate('general.urgency', 'ISU', 'Urgency'),
                  layout: 'hbox',
                  items: [
                     {
                        xtype: 'numberfield',
                        itemId: 'priority-urgency',
                        width: 92,
                        name: 'priority.urgency',
                        value: defaultUrgency,
                        minValue: 1,
                        maxValue: 50,
                        listeners: {
                           change: function () {
                              me.changePriority();
                           },
                           blur: me.numberFieldValidation
                        }

                     },
                     {
                        xtype: 'numberfield',
                        itemId: 'priority-impact',
                        labelWidth: 50,
                        width: 157,
                        name: 'priority.impact',
                        fieldLabel: Uni.I18n.translate('general.impact', 'ISU', 'Impact'),
                        value: defaultImpact,
                        minValue: 1,
                        maxValue: 50,
                        margin: '0 0 0 20',
                        listeners: {
                           change: function () {
                              me.changePriority();
                           },
                           blur: me.numberFieldValidation
                        }

                     }
                  ]
               },
               {
                    xtype: 'fieldcontainer',
                    itemId: 'issues-creation-rules-edit-field-container-due-date',
                    fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                    layout: 'hbox',
                    items: [
                        {
                            itemId: 'dueDateTrigger',
                            xtype: 'radiogroup',
                            name: 'dueDateTrigger',
                            formBind: false,
                            columns: 1,
                            vertical: true,
                            width: 100,
                            defaults: {
                                name: 'dueDate',
                                formBind: false,
                                submitValue: false
                            },
                            items: [
                                {
                                    itemId: 'noDueDate',
                                    boxLabel: Uni.I18n.translate('issueCreationRules.noDueDate', 'ISU', 'No due date'),
                                    inputValue: false,
                                    checked: true
                                },
                                {
                                    itemId: 'dueIn',
                                    boxLabel: Uni.I18n.translate('general.title.dueIn', 'ISU', 'Due in'),
                                    inputValue: true,
                                    margin: '7 0 0 0',
                                    checked: false
                                }
                            ],
                            listeners: {
                                change: {
                                    fn: Ext.bind(me.dueDateTrigger, me)
                                }
                            }
                        },
                        {
                            itemId: 'dueDateValues',
                            xtype: 'container',
                            name: 'dueDate',
                            margin: '30 0 10 0',
                            layout: {
                                type: 'hbox'
                            },
                            items: [
                                {
                                    itemId: 'dueIn.number',
                                    xtype: 'numberfield',
                                    name: 'dueIn.number',
                                    minValue: 1,
                                    width: 60,
                                    margin: '0 10 0 0',
                                    listeners: {
                                        focus: {
                                            fn: Ext.bind(me.chooseDueInRadio, me)
                                        },
                                        change: {
                                            fn: function (field, newValue) {
                                                if (newValue < 0) {
                                                    field.setValue(Math.abs(newValue));
                                                } else if (newValue > Math.pow(10,12)) {
                                                    field.setValue(Math.pow(10,12));
                                                }
                                            }
                                        }
                                    }
                                },
                                {
                                    itemId: 'dueIn.type',
                                    xtype: 'combobox',
                                    name: 'dueIn.type',
                                    store: 'Isu.store.DueinTypes',
                                    queryMode: 'local',
                                    displayField: 'displayValue',
                                    valueField: 'name',
                                    editable: false,
                                    width: 100,
                                    listeners: {
                                        focus: {
                                            fn: Ext.bind(me.chooseDueInRadio, me)
                                        }
                                    }
                                }
                            ]
                        }
                    ]
               },
               {
                  xtype: 'combobox',
                  itemId: 'mi-workgroup-issue-assignee',
                  fieldLabel: Uni.I18n.translate('general.workgroup', 'ISU', 'Workgroup'),
                  queryMode: 'local',
                  valueField: 'id',
                  displayField: 'name',
                  name: 'assignToWorkgroupId',
                  store: 'Isu.store.IssueWorkgroupAssignees',
                  emptyText: Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned'),
                  msgTarget: 'under',
                  editable: false,
                  listeners: {
                     render: function () {
                        this.store.load();
                     },
                     change: function (combo, newValue) {
                        this.up('form').down('#mi-user-issue-assignee').fireEvent('workgroupChanged', newValue);
                     },
                     select: function (combo, newValue) {
                        this.up('form').down('#mi-user-issue-assignee').fireEvent('workgroupChanged', newValue[0].get('id'));
                     }
                  }
               },
               {
                  xtype: 'issues-user-assignee-combo',
                  itemId: 'mi-user-issue-assignee',
                  fieldLabel: Uni.I18n.translate('general.user', 'ISU', 'User'),
                  name: 'assignToUserId',
                  queryMode: 'local',
                  valueField: 'id',
                  displayField: 'name',
                  editable: false,
                  store: 'Isu.store.UserList',
                  emptyText: Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned'),
                  msgTarget: 'under'
               },
               {
                    itemId: 'comment',
                    xtype: 'textareafield',
                    name: 'comment',
                    fieldLabel: Uni.I18n.translate('general.comment', 'ISU', 'Comment'),
                    emptyText: Uni.I18n.translate('general.provideComment','ISU','Provide a comment (optionally)'),
                    height: 160
               },
               {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: ' ',
                    defaultType: 'button',
                    hidden: me.bulkAction,
                    items: [
                        {
                            itemId: 'actionOperation',
                            ui: 'action',
                            text: Uni.I18n.translate('general.save', 'ISU', 'Save'),
                            action: 'saveIssueAction'
                        },
                        {
                            itemId: 'cancel',
                            text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                            action: 'cancel',
                            ui: 'link',
                            href: me.returnLink
                        }
                    ]
               }

            ];

        me.callParent(arguments);

    },
    changePriority: function()
    {
        var me = this,
            labelPriority = me.down('#priority-label'),
            numUrgency = me.down('[name=priority.urgency]'),
            numUrgencyValue = numUrgency.value,
            numImpact = me.down('[name=priority.impact]'),
            numImpactValue = numImpact.value,
            priorityValue,
            priorityLabel;

        if (numUrgencyValue < 0) {
            numUrgency.setValue(Math.abs(numUrgencyValue));
        }

        if (numImpactValue < 0) {
            numImpact.setValue(Math.abs(numImpactValue));
        }

        priorityValue = Math.abs(numUrgencyValue) +  Math.abs(numImpactValue);

        var priority = priorityValue / 10;
        if (priorityValue > 100) {
            priority = 10;
            priorityValue = 100;
        }

        priorityLabel = (priority <= 2) ? Uni.I18n.translate('issue.priority.veryLow', 'ISU', 'Very low') :
            (priority <= 4) ? Uni.I18n.translate('issue.priority.low', 'ISU', 'Low') :
                (priority <= 6) ? Uni.I18n.translate('issue.priority.medium', 'ISU', 'Medium') :
                    (priority <= 8) ? Uni.I18n.translate('issue.priority.high', 'ISU', 'High') :
                        Uni.I18n.translate('issue.priority.veryHigh', 'ISU', 'Very high');


        labelPriority.setText(priorityLabel + ' (' + priorityValue +')');

    },
    chooseDueInRadio: function () {
        var me = this;

        me.down('#dueDateTrigger').setValue({dueDate: true});
    },
    dueDateTrigger: function (radioGroup, newValue) {
        var me = this,
            dueDateValues = me.down('[name=dueDateValues]'),
            dueInNumberField = me.down('[name=dueIn.number]'),
            dueInTypeField = me.down('[name=dueIn.type]');

        if (!newValue.dueDate) {
            dueInNumberField.reset();
            dueInTypeField.setValue(dueInTypeField.getStore().getAt(0).get('name'));
        }
    },
    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
        if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }

});