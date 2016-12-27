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
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'life-cycle-transition-container',
                fieldLabel: Uni.I18n.translate('general.transition', 'IMT', 'Transition'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'combo-returned-record-data',
                        name: 'id',
                        itemId: 'life-cycle-transition-combo',
                        store: 'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitions',
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
                        margin: '0 0 0 20',
                        handler: Ext.bind(me.resetLifeCycleTransition, me)
                    }
                ]
            },
            {
                xtype: 'date-time',
                itemId: 'transition-date',
                fieldLabel: Uni.I18n.translate('general.transitionDate', 'IMT', 'Transition date'),
                name: 'transitionDate',
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
            }
        ];

        me.callParent(arguments);
    },

    onLifeCycleTransitionChange: function (combo, newValue) {
        var me = this,
            transitionDateField = me.down('#transition-date'),
            metrologyConfiguration = me.usagePoint.get('metrologyConfiguration'),
            possibleTransitionDates = [me.usagePoint.get('installationTime') || new Date().getTime()];

        if (metrologyConfiguration && !Ext.isEmpty(metrologyConfiguration.meterRoles)) {
            Ext.Array.each(metrologyConfiguration.meterRoles, function (meterRole) {
                possibleTransitionDates.push(meterRole.activationTime);
            });
        }
        Ext.suspendLayouts();
        me.down('#reset-life-cycle-transition').setDisabled(!newValue);
        transitionDateField.setValue(_.max(possibleTransitionDates));
        transitionDateField.setVisible(!!newValue);
        Ext.resumeLayouts(true);
    },

    resetLifeCycleTransition: function () {
        var me = this;

        me.down('#life-cycle-transition-combo').reset();
    }
});