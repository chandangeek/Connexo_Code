Ext.define('Cfg.model.ValidationRuleSetVersion', {
    extend: 'Ext.data.Model',

    fields: [
        'id',
        'name',
		'description',
        'startDate',
        {
            name: 'startDateFormatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.startDate && (data.startDate !== 0)) {
                    result = moment(data.startDate).format('ddd, DD MMM YYYY HH:mm:ss');
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.startDate));
                } else {
                    result = Uni.I18n.translate('validationTasks.general.notStart', 'CFG', '-')
                }
                return result;
            }
        },
        {
            name: 'action',
            persist: false
        },
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
        //urlTpl: '/api/val/validation/{ruleSetId}/rules',
        urlTpl: '/api/val/validation/{ruleSetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        },
        setUrl: function (ruleSetId, versionId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId);
        },
        timeout: 300000
    }
});

