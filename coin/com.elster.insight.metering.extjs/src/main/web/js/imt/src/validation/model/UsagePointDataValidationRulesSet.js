Ext.define('Imt.validation.model.UsagePointDataValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isActive', type: 'boolean'},
//        {name: 'numberOfInactiveRules', type: 'int'},
//        {name: 'numberOfRules', type: 'int'},
        {name: 'numberOfVersions', type: 'int'},
        {name: 'hasCurrent', type: 'boolean'},
        {name: 'version'},
        {name: 'startDate'},
        {name: 'endDate'},
        {name: 'usagePoint', type: 'auto'}
//        {
//            name: 'numberOfActiveRules',
//            persist: false,
//            mapping: function (data) {
//                return data.numberOfRules - data.numberOfInactiveRules;
//            }
//        },
//		{
//            name: 'activeVersion',
//            convert: function (value, record) {
//
//                var result, startDate, endDate, numberOfVersions, hasCurrent;
//
//                numberOfVersions = record.get('numberOfVersions');
//                hasCurrent = record.get('hasCurrent');
//                startDate = record.get('startDate');
//                endDate = record.get('endDate');
//                if (numberOfVersions === 0 || hasCurrent === false){
//                    result = '-';
//                } else if (startDate && endDate) {
//                    result = Ext.String.format(Uni.I18n.translate('validationResults.version.fromx', 'IMT', 'From {0}',[Uni.DateTime.formatDateTimeLong(new Date(startDate))])+ ' - ' +
//                    Uni.I18n.translate('validationResults.version.untilx', 'IMT', 'Until {0}',[Uni.DateTime.formatDateTimeLong(new Date(endDate))]));
//                } else if (startDate) {
//                    result = Ext.String.format(Uni.I18n.translate('validationResults.version.fromx', 'IMT', 'From {0}'), Uni.DateTime.formatDateTimeLong(new Date(startDate)));
//                } else if (endDate) {
//                    result = Ext.String.format(Uni.I18n.translate('validationResults.version.untilx', 'IMT', 'Until {0}',[Uni.DateTime.formatDateTimeLong(new Date(endDate))]));
//                }else {
//                    result = Ext.String.format(Uni.I18n.translate('validationResults.version.notStart', 'IMT', 'Always'))
//                }
//
//				return result;
//            }
//       }
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/validationrulesets',
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'rulesets',
            totalProperty: 'total'
        }
    }
});