Ext.define('Cfg.model.ValidationRuleSetVersion', {
    extend: 'Ext.data.Model',

    fields: [
        'id',
        'name',
		'description',
        'startPeriod',
        {
            name: 'startPeriodFormatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.startPeriod && (data.startPeriod !== 0)) {
                    result = moment(data.startPeriod).format('ddd, DD MMM YYYY HH:mm:ss');
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.startPeriod));
                } else {
                    result = Uni.I18n.translate('validationTasks.general.notStart', 'CFG', '-')
                }
                return result;
            }
        },
        'action',
        {
            name: 'versionName',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/validation/rulesets/' + data.ruleSet.id + '/versions/' + data.id + '">' + data.name + '</a>';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}/rules',
        reader: {
            type: 'json',
            root: 'rules'
        },
        setUrl: function (ruleSetId, versionId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId);
        },
        timeout: 300000
    }
});

