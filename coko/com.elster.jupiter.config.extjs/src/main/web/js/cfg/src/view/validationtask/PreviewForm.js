Ext.define('Cfg.view.validationtask.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-tasks-preview-form',

    requires: [        
        'Uni.form.field.Duration',
        'Cfg.view.validationtask.DataSourcesPreviewContainer',
        'Dxp.store.TaskLogLevels'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    appName: null,

    initComponent: function () {
        var me = this,
            logLevelsStore = Ext.getStore('Dxp.store.TaskLogLevels');

        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.logLevel', 'CFG', 'Log level'),
                name: 'logLevelId',
                labelWidth: 250,
                renderer: function(value) {
                    if (Ext.isEmpty(value)) {
                        return '-';
                    }
                    var storeIndex = logLevelsStore.findExact('id', value);
                    return storeIndex === -1 ? '-' : logLevelsStore.getAt(storeIndex).get('displayValue');
                }
            },
            {
                xtype: 'cfg-data-sources-preview-container'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('validationTasks.general.schedule', 'CFG', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [ 
					{
                        fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'Recurrence'),
                        name: 'recurrence'
                    },				
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.lastRun', 'CFG', 'Last run'),
                        name: 'lastRun_formatted'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'CFG', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.reason', 'CFG', 'Reason'),
                        itemId: 'lbl-reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.nextRun', 'CFG', 'Next run'),
                        name: 'nextRun_formatted'
                    }
                ]
            }         
        ];
        me.callParent();
    }
});
