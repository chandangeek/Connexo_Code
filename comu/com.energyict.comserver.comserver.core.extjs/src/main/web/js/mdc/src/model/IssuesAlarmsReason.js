/**
 * Created by H251853 on 9/14/2017.
 */
Ext.define('Mdc.model.IssuesAlarmsReason', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        {
            name: 'issueType',
            convert: function (value, record) {
                if (value == 'ALM')
                    return Uni.I18n.translate('issueType.alarms', 'MDC', 'Alarms');
                else if ((value == 'DVI') || (value == 'DCI'))
                    return Uni.I18n.translate('issueType.issues', 'MDC', 'Issues');
                return value;
            }
        }
    ]
});