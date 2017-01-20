Ext.define('Imt.purpose.util.TooltipRenderer', {
    singleton: true,
    prepareIcon: function (record) {
        var readingQualitiesPresent = !Ext.isEmpty(record.get('readingQualities')),            
            tooltipContent = '',
            group = [
                {
                    title: Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality'),
                    items: []
                },
                {
                    title: Uni.I18n.translate('general.MDCQuality', 'IMT', 'MDC quality'),
                    items: []
                },
                {
                    title: Uni.I18n.translate('general.MDMQuality', 'IMT', 'MDM quality'),
                    items: []
                },
                {
                    title: Uni.I18n.translate('general.thirdPartyQuality', 'IMT', 'Third party quality'),
                    items: []
                }
            ],
            icon = '';

        if (readingQualitiesPresent) {
            Ext.Array.forEach(record.get('readingQualities'), function (readingQuality) {
                var cimCode = readingQuality.cimCode,
                    indexName = readingQuality.indexName;

                switch (cimCode.slice(0,2)) {
                    case '1.':
                        group[0].items.push(indexName);
                        break;
                    case '2.':
                        group[1].items.push(indexName);
                        break;
                    case '3.':
                        group[2].items.push(indexName);
                        break;
                    case '4.':
                    case '5.':
                        group[3].items.push(indexName);
                        break;
                }
            });

            Ext.Object.each(group, function(key, value) {
                if (value.items.length) {
                    tooltipContent += '<b>' + value.title + '</b><br>';
                    tooltipContent += addCategoryAndNames(value.items) + '<br>';
                }
            });

            if (tooltipContent.length > 0) {
                tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'IMT', 'View reading quality details for more information.');
                icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtip="' + tooltipContent + '"></span>';
            }
        }

        return icon;

        function addCategoryAndNames (qualities) {
            var result = '';
            Ext.Array.each(qualities, function (q) {
                result += q + '<br>';
            });
            return result;
        }
    }
});
