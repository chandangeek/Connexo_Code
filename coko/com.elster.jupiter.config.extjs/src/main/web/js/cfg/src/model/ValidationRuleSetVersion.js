Ext.define('Cfg.model.ValidationRuleSetVersion', {
    extend: 'Ext.data.Model',

    fields: [
        'id',
        {
            name: 'versionName',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

                startDate = data.startDate;
                endDate = data.endDate;
                if (startDate && endDate) {
                    result = Uni.I18n.translate('validationTasks.general.from', 'CFG', 'From') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(startDate)) + ' - ' +
                    Uni.I18n.translate('validationTasks.general.until', 'CFG', 'Until') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(endDate));
                } else if (data.startDate) {
                    result = Uni.I18n.translate('validationTasks.general.from', 'CFG', 'From') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(startDate));
                } else if (data.endDate) {
                    result = Uni.I18n.translate('validationTasks.general.until', 'CFG', 'Until') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(endDate));
                }else {
                    result = Uni.I18n.translate('validationTasks.general.notStart', 'CFG', 'Always')
                }

                return '<a href="#/administration/validation/rulesets/' + data.ruleSet.id + '/versions/' + data.id + '">' + result + '</a>';
            }
        },
        {
            name: 'name',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

                startDate = data.startDate;
                endDate = data.endDate;
                if (startDate && endDate) {
                    result = Uni.I18n.translate('validationTasks.general.from', 'CFG', 'From') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(startDate)) + ' - ' +
                             Uni.I18n.translate('validationTasks.general.until', 'CFG', 'Until') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(endDate));
                } else if (data.startDate) {
                    result = Uni.I18n.translate('validationTasks.general.from', 'CFG', 'From') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(startDate));
                } else if (data.endDate) {
                    result = Uni.I18n.translate('validationTasks.general.until', 'CFG', 'Until') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(endDate));
                }else {
                    result = Uni.I18n.translate('validationTasks.general.notStart', 'CFG', 'Always')
                }
                return result;
            }
        },
		'description',
        {
            name: 'startDate',
            mapping: function (data) {
                var result;
                if (data.startDate && (data.startDate !== 0) && (data.startDate !== '')) {
                    result = data.startDate;
                } else {
                    result = null;
                }
                return result;
            }
        },
        {
            name: 'startDateFormatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.startDate && (data.startDate !== 0) && (data.startDate !== '')) {
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
            name: 'status',
            persist: false
        },
        {
            name: 'ruleSetId',
            persist: false,
            mapping: function (data) {
                return data.ruleSet.id;
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}/versions',
        cloneUrlTpl: '/api/val/validation/{ruleSetId}/versions/{versionId}/clone',
        reader: {
            type: 'json',
            root: 'versions'
        },
        setUrl: function (ruleSetId, versionId, clone) {
            if (clone){
                this.url = this.cloneUrlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId);
            } else {
                this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId);
            }

        },
        timeout: 300000
    }
});

