/**
 * Created by H251853 on 8/10/2017.
 */

Ext.define('Isu.view.issues.SnoozeBulkForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Isu.model.Issue'
    ],

    defaultDate: null,
    alias: 'widget.snooze-bulk-form',
    returnLink: null,
    ui: 'large',

    initComponent: function () {
        var me = this,
            tomorrowMidnight = new Date();
        tomorrowMidnight.setHours(24, 0, 0, 1);
        me.defaultDate = tomorrowMidnight;
        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: 400,
                height: 48,
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'issue-snooze-until-date',
                        layout: 'hbox',
                        name: 'until',
                        dateConfig: {
                            fieldLabel: Uni.I18n.translate('general.until', 'ISU', 'Until'),
                            labelWidth: 25,
                            margin: '0 0 0 1',
                            width: 156,
                            allowBlank: false,
                            value: me.defaultDate,
                            minValue: moment().toDate(),
                            editable: false,
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            fieldLabel: Uni.I18n.translate('general.at', 'ISU', 'at'),
                            labelWidth: 10,
                            margin: '0 0 0 0',
                            value: me.defaultDate.getHours()
                        },
                        minutesConfig: {
                            width: 55,
                            value: me.defaultDate.getMinutes()
                        },
                        listeners: {
                            focus: {
                                fn: function () {
                                    me.down('#issue-snooze-until-date').setValue(true);
                                }
                            }
                        }
                    }

                ],
                action: 'applyAction'
            }
        ];

        me.callParent(arguments);
    }
});