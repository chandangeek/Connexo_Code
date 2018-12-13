Ext.define('Imt.usagepointmanagement.view.forms.LifeCycleTransition', {
    extend: 'Ext.form.Panel',
    alias: 'widget.life-cycle-transition-info-form',
    requires: [
        'Imt.usagepointmanagement.view.StepDescription'
    ],

    usagePoint: null,

    defaults: {
        labelWidth: 260
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'step-description',
                itemId: 'link-metrology-configuration-with-meters-step-description',
                text: Uni.I18n.translate('usagepoint.wizard.lifeCycleTransitionStep.description', 'IMT', 'The selected usage point life cycle transition will be triggered after the usage point creation.')
            },
            {
                itemId: 'life-cycle-transition-info-warning',
                xtype: 'uni-form-error-message',
                width: 595,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'life-cycle-transition-container',
                fieldLabel: Uni.I18n.translate('general.transition', 'IMT', 'Transition'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'combobox',
                        name: 'id',
                        itemId: 'life-cycle-transition-combo',
                        store: 'Imt.usagepointmanagement.store.UsagePointTransitions',
                        displayField: 'name',
                        valueField: 'id',
                        queryMode: 'local',
                        forceSelection: true,
                        emptyText: Uni.I18n.translate('general.placeholder.selectTransition', 'IMT', 'Select a transition...'),
                        width: 320,
                        listeners: {
                            change: Ext.bind(me.onLifeCycleTransitionChange, me)
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'reset-life-cycle-transition',
                        iconCls: 'icon-rotate-ccw3',
                        tooltip: Uni.I18n.translate('general.reset', 'IMT', 'Reset'),
                        disabled: true,
                        width: 30,
                        margin: '0 0 0 5',
                        handler: Ext.bind(me.resetLifeCycleTransition, me)
                    }
                ]
            },
            {
                xtype: 'date-time',
                itemId: 'transition-date',
                fieldLabel: Uni.I18n.translate('general.transitionDate', 'IMT', 'Transition date'),
                name: 'effectiveTimestamp',
                required: true,
                hidden: true,
                valueInMilliseconds: true,
                layout: 'hbox',
                dateConfig: {
                    width: 110
                },
                dateTimeSeparatorConfig: {
                    html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
                    style: 'color: #686868'
                },
                hoursConfig: {
                    width: 60
                },
                minutesConfig: {
                    width: 60
                }
            },
            {
                xtype: 'component',
                itemId: 'transition-date-field-errors',
                cls: 'x-form-invalid-under',
                style: {
                    'white-space': 'normal',
                    'padding': '0px 0px 10px 275px'
                },
                hidden: true
            },
            {
                xtype: 'property-form',
                itemId: 'transition-property-form',
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    width: 320,
                    resetButtonHidden: true
                }
            }
        ];

        me.callParent(arguments);
    },

    onLifeCycleTransitionChange: function (combo, newValue) {
        var me = this,
            metrologyConfiguration = me.usagePoint.get('metrologyConfiguration'),
            possibleTransitionDates = [me.usagePoint.get('installationTime') || new Date().getTime()],
            transitionRecord = combo.findRecordByValue(newValue),
            resetTransitionButton = me.down('#reset-life-cycle-transition'),
            transitionDateField = me.down('#transition-date'),
            transitionPropertyForm = me.down('#transition-property-form');

        if (metrologyConfiguration && !Ext.isEmpty(metrologyConfiguration.meterRoles)) {
            Ext.Array.each(metrologyConfiguration.meterRoles, function (meterRole) {
                possibleTransitionDates.push(meterRole.activationTime);
            });
        }

        Ext.suspendLayouts();
        if (transitionRecord) {
            var effectiveTimestamp = moment(_.max(possibleTransitionDates)).add(1, 'm');
            resetTransitionButton.enable();
            transitionDateField.show();
            me.down('#transition-date-field-errors').hide();
            // CXO-8275 - time can be set in the past - for testing reasons
            //transitionDateField.down('#date-time-field-date').setMinValue(moment(effectiveTimestamp).startOf('day').toDate());
            transitionRecord.set('effectiveTimestamp', effectiveTimestamp);
            me.loadRecord(transitionRecord);
            transitionPropertyForm.show();
            transitionPropertyForm.loadRecord(transitionRecord);
        } else {
            resetTransitionButton.disable();
            transitionDateField.hide();
            transitionPropertyForm.hide();
        }
        Ext.resumeLayouts(true);
    },

    resetLifeCycleTransition: function () {
        var me = this;

        me.down('#life-cycle-transition-combo').reset();
    },

    getRecord: function () {
        var me = this,
            transitionField = me.down('#life-cycle-transition-combo'),
            transitionRecord = transitionField.findRecordByValue(transitionField.getValue()),
            record = transitionRecord ? me.callParent(arguments) : null;

        if (record) {
            me.updateRecord();
            me.down('#transition-property-form').updateRecord();
        }

        return record ? record.getProxy().getWriter().getRecordData(record) : null;
    },

    markInvalid: function (errors) {
        var me = this;

        Ext.suspendLayouts();
        me.getForm().markInvalid(me.mapErrors(errors));
        me.down('#transition-property-form').markInvalid(errors);
        Ext.resumeLayouts(true);
    },

    clearInvalid: function () {
        var me = this;

        Ext.suspendLayouts();
        me.getForm().clearInvalid();
        me.down('#transition-property-form').clearInvalid();
        me.down('#transition-date-field-errors').hide();
        Ext.resumeLayouts(true);
    },

    mapErrors: function (errors) {
        var map = {},
            errMsg = [],
            errorsField = this.down('#transition-date-field-errors');

        Ext.Array.each(errors, function (error) {

            if (Ext.String.startsWith(error.id, 'effectiveTimestamp')) {
                error.id = 'effectiveTimestamp.transition-date-field-errors';
                errMsg.push(error.msg);
                if (!map[error.id]) {
                    map[error.id] = {
                        id: error.id
                    };
                } else {
                    map[error.id].msg.push(error.msg);
                }
            } else {
                if (!map[error.id]) {
                    map[error.id] = {
                        id: error.id,
                        msg: [' '+error.msg]
                    };
                }
            }
            errorsField.show();
            errorsField.update(' '+errMsg.join('<br> '));

        });

        return _.values(map);
    }
});