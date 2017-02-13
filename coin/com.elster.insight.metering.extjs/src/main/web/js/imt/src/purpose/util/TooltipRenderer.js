Ext.define('Imt.purpose.util.TooltipRenderer', {
    singleton: true,
    prepareIcon: function (record) {
        var readingQualities = record.get('readingQualities'),
            readingQualitiesPresent = !Ext.isEmpty(readingQualities),
            tooltipContent = '',
            groups = [
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
            readingQualities.sort(function (a, b) {
                if (a.indexName > b.indexName) {
                    return 1;
                }
                if (a.indexName < b.indexName) {
                    return -1;
                }
                return 0;
            });
            Ext.Array.forEach(readingQualities, function (readingQuality) {
                var cimCode = readingQuality.cimCode,
                    indexName = readingQuality.indexName;

                switch (cimCode.slice(0,2)) {
                    case '1.':
                        groups[0].items.push(indexName);
                        break;
                    case '2.':
                        groups[1].items.push(indexName);
                        break;
                    case '3.':
                        groups[2].items.push(indexName);
                        break;
                    case '4.':
                    case '5.':
                        groups[3].items.push(indexName);
                        break;
                }
            });

            Ext.Array.each(groups, function (group) {
                if (group.items.length) {
                    tooltipContent += '<b>' + group.title + '</b><br>';
                    tooltipContent += addCategoryAndNames(group.items) + '<br>';
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
