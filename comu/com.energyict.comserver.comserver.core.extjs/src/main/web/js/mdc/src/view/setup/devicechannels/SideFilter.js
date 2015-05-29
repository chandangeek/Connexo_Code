Ext.define('Mdc.view.setup.devicechannels.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.LoadProfileDataDurations'
    ],
    alias: 'widget.deviceLoadProfileChannelDataSideFilter',
    itemId: 'deviceLoadProfileChannelDataSideFilter',
    ui: 'medium',
    width: 288,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'deviceLoadProfileChannelDataFilterForm',
            ui: 'filter',
            items: [
                    me.contentName == 'block' ? null : {
                    xtype: 'fieldcontainer',
                    itemId: 'dateContainer',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                    labelAlign: 'top',
                    defaults: {
                        width: '100%'
                    },
                    items: [
                        {
                            xtype: 'date-time',
                            itemId: 'endOfInterval',
                            name: 'intervalStart',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                            labelAlign: 'top',
                            labelStyle: 'font-weight: normal',
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                            }
                        }
                    ]
                },
                {
                    xtype: 'combobox',
                    itemId: 'sideFilterDuration',
                    name: 'duration',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                    labelAlign: 'top',
                    store: 'Mdc.store.LoadProfileDataDurations',
                    displayField: 'localizeValue',
                    valueField: 'id',
                    queryMode: 'local',
                    anchor: '100%'
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'suspectContainer',
                    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.result', 'MDC', 'Validation result'),
                    labelAlign: 'top',
                    defaultType: 'checkboxfield',
                    items: [
                        {
                            itemId: 'suspect',
                            inputValue: 'suspect',
                            name: 'onlySuspect',
                            boxLabel: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                            afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                        },
                            me.contentName == 'block' ? null : {
                            itemId: 'nonSuspect',
                            inputValue: 'nonSuspect',
                            name: 'onlyNonSuspect',
                            padding: '-10 0 0 0',
                            boxLabel: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect')
                        }
                    ]
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            itemId: 'deviceLoadProfileDataFilterApplyBtn',
                            ui: 'action',
                            text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                            action: 'filter'
                        },
                        {
                            itemId: 'deviceLoadProfileDataFilterResetBtn',
                            text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                            action: 'reset'
                        }
                    ]
                }
            ]
        };
        me.callParent(arguments)
    }
});