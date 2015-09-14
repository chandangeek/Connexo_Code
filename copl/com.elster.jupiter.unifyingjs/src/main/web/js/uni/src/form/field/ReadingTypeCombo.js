/**
 * @class Uni.form.field.ReadingTypeCombo
 */
Ext.define('Uni.form.field.ReadingTypeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.reading-type-combo',
    requires: ['Ext.button.Button'],
    fieldLabel: Uni.I18n.translate('general.readingType', 'UNI', 'Reading type'),
    emptyText: Uni.I18n.translate('readingType.emptyText', 'UNI', 'Select reading type'),
    displayField: 'aliasName',
    valueField: 'mRID',
    triggerAction: 'all',
    queryMode: 'local',
    editable: false,
    showTimeAttribute: true,
    tpl: Ext.create('Ext.XTemplate',
        '<tpl for=".">',
            '<div class="x-boundlist-item">',
                '<tpl if="aliasName.length &gt; 0">',
                    '<tpl if="names">',
                        '<tpl if="names.timeAttribute.length &gt; 0">',
                            '[{names.timeAttribute}] ',
                        '</tpl>',
                    '</tpl>',
                    '{aliasName}',
                    '<tpl if="names">',
                        '<tpl if="names.unitOfMeasure.length &gt; 0">',
                            ' ({names.unitOfMeasure})',
                        '</tpl>',
                        '<tpl if="names.phase.length &gt; 0">',
                            ' {names.phase}',
                        '</tpl>',
                        '<tpl if="names.timeOfUse.length &gt; 0">',
                            ' {names.timeOfUse}',
                        '</tpl>',
                    '</tpl>',
                '<tpl else>',
                    '{mRID}',
                '</tpl>',
            '</div>',
        '</tpl>'
    ),
    displayTpl: Ext.create('Ext.XTemplate',
        '<tpl for=".">',
            '<tpl if="aliasName.length &gt; 0">',
                '<tpl if="names">',
                    '<tpl if="names.timeAttribute.length &gt; 0">',
                        '[{names.timeAttribute}] ',
                    '</tpl>',
                '</tpl>',
                '{aliasName}',
                '<tpl if="names">',
                    '<tpl if="names.unitOfMeasure.length &gt; 0">',
                        ' ({names.unitOfMeasure})',
                    '</tpl>',
                    '<tpl if="names.phase.length &gt; 0">',
                        ' {names.phase}',
                    '</tpl>',
                    '<tpl if="names.timeOfUse.length &gt; 0">',
                        ' {names.timeOfUse}',
                    '</tpl>',
                '</tpl>',
            '<tpl else>',
                '{mRID}',
            '</tpl>',
        '</tpl>'
    ),
    listeners: {
        change: function (field, newValue) {
            field.el.down('a') && field.el.down('a').setVisible(!!newValue && newValue.length > 0);
        }
    },
    initComponent: function () {
        this.callParent();
        var me = this;
        Ext.defer(function () {
            new Ext.button.Button({
                renderTo: me.el.down('.x-form-item-body'),
                tooltip: Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info'),
                hidden: true,
                iconCls: 'uni-icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    textDecoration: 'none !important',
                    position: 'absolute',
                    top: '5px',
                    right: '-65px'
                },
                handler: function () {
                    me.handler();
                }
            });
            me.updateLayout();
        }, 10);
    },
    getReadingTypeName: function (readingType) {
        if (!readingType) return this.emptyText;
        var assembledName = '';
        var alias = readingType.aliasName ? (' ' + readingType.aliasName) : '';
        if (readingType.names && Ext.isObject(readingType.names)) {
            assembledName +=
                    ((readingType.names.timeAttribute && this.showTimeAttribute) ? (' [' + readingType.names.timeAttribute + ']') : '')
                    + alias
                    + (readingType.names.unitOfMeasure ? (' (' + readingType.names.unitOfMeasure + ')') : '')
                    + (readingType.names.phase ? (' ' + readingType.names.phase ) : '')
                    + (readingType.names.timeOfUse ? (' ' + readingType.names.timeOfUse) : '');
        } else {
            assembledName += alias;
        }
        return assembledName || readingType.mRID;
    },
    handler: function () {
        if (this.valueModels[0]) {
            var selectedReadingType = this.valueModels[0].getData(),
                widget = Ext.widget('readingTypeDetails');
            widget.setTitle('<span style="margin: 10px 0 0 10px">' + this.getReadingTypeName(selectedReadingType) + '</span>');
            var tpl = new Ext.XTemplate(
                '<table style="width: 100%; margin: 30px 10px">',
                    '<tr>',
                        '<td colspan="2">',
                            '<table style="width: 100%; margin-bottom: 30px">',
                                '<tr>',
                                    '<td style="width: 30%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.name', 'UNI', 'Reading type name') + '</td>',
                                    '<td style="width: 70%; text-align: left; padding-bottom: 10px">' + this.getReadingTypeName(selectedReadingType) + '</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 30%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.cimCode', 'UNI', 'CIM code') + '</td>',
                                    '<td style="width: 70%; text-align: left; padding-bottom: 10px">{mRID}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 30%; text-align: right; font-weight: bold; padding-right: 20px">' + Uni.I18n.translate('readingType.description', 'UNI', 'Description') + '</td>',
                                    '<td style="width: 70%; text-align: left">{name}</td>',
                                '</tr>',
                            '</table>',
                        '</td>',
                    '</tr>',
                    '<tr>',
                        '<td colspan="2" style="padding-bottom: 20px; font-weight: bold; font-size: 1.5em; color: grey">' + Uni.I18n.translate('readingType.cimCodeDetails', 'UNI', 'CIM code details') + '</td>',
                    '</tr>',
                    '<tr>',
                        '<td style="width: 50%; vertical-align: top">',
                            '<table style="width: 100%">',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timePeriodOfInterest', 'UNI', 'Time-period of interest') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{macroPeriod}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.dataQualifier', 'UNI', 'Data qualifier') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{aggregate}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.time', 'UNI', 'Time') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{measuringPeriod}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.accumulation', 'UNI', 'Accumulation') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{accumulation}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.directionOfFlow', 'UNI', 'Direction of flow') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{flowDirection}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.commodity', 'UNI', 'Commodity') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{commodity}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.measurementKind', 'UNI', 'Kind') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{measurementKind}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.interharmonicNumerator', 'UNI', 'Interharmonic numerator') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{interHarmonicNumerator}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.interharmonicDenominator', 'UNI', 'Interharmonic denominator') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{interHarmonicDenominator}</td>',
                                '</tr>',
                            '</table>',
                        '</td>',
                        '<td style="width: 50%; vertical-align: top">',
                            '<table style="width: 100%">',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.argumentNumerator', 'UNI', 'Argument numerator') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{argumentNumerator}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.argumentDenominator', 'UNI', 'Argument denominator') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{argumentDenominator}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timeOfUse', 'UNI', 'Time of use') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{tou}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.criticalPeakPeriod', 'UNI', 'Critical peak period') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{cpp}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.comsumptionTier', 'UNI', 'Consumption tier') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{consumptionTier}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.phase', 'UNI', 'Phase') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{phases}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.multiplier', 'UNI', 'Multiplier') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{metricMultiplier}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.unitOfMeasure', 'UNI', 'Unit of measure') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{unit}</td>',
                                '</tr>',
                                '<tr>',
                                    '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.currency', 'UNI', 'Currency') + '</td>',
                                    '<td style="width: 50%; text-align: left; padding-bottom: 10px">{currency}</td>',
                                '</tr>',
                            '</table>',
                        '</td>',
                    '</tr>',
                '</table>'
            );
            tpl.overwrite(widget.down('panel').body, selectedReadingType);
            widget.show();
        }
    }
});