/**
 * @class Uni.view.window.ReadingTypeWizard
 */
Ext.define('Uni.view.window.ReadingTypeWizard', {
    extend: 'Uni.view.window.Wizard',

    requires: [
    ],

    minWidth: 400,
    minHeight: 200,

    title: Uni.I18n.translate('window.readingtypewizard.title', 'UNI', 'Reading type wizard'),

    description: {
        xtype: 'component',
        html: ''
    },

    steps: [
        {
            title: Uni.I18n.translate('window.readingtypewizard.introduction', 'UNI', 'Introduction'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: Uni.I18n.translate('window.readingtypewizard.introduction.content', 'UNI',
                        '<h3>Introduction</h3>')
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.macroperiod', 'UNI', 'Macro period'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Macro period</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.dataaggregation', 'UNI', 'Data aggregation'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Data aggregation</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.measurementperiod', 'UNI', 'Measurement period'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Measurement period</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.dataaccumulation', 'UNI', 'Data accumulation'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Data accumulation</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.directionofflow', 'UNI', 'Direction of flow'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Direction of flow</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.commodity', 'UNI', 'Commodity'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Commodity</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.measurementkind', 'UNI', 'Measurement kind'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Measurement kind</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.interharmonics', 'UNI', 'Interharmonics'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Interharmonics</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.argument', 'UNI', 'Argument'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Argument</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.timeofuse', 'UNI', 'Time of use'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Time of use</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.criticalpeakperiod', 'UNI', 'Critical peak period'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Critical peak period</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.consumptiontier', 'UNI', 'Consumption tier'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Consumption tier</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.phase', 'UNI', 'Phase'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Phase</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.multiplier', 'UNI', 'Multiplier'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Multiplier</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.unitofmeasure', 'UNI', 'Unit of measure'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Unit of measure</h3>'
                }
            ]
        },
        {
            title: Uni.I18n.translate('window.readingtypewizard.currency', 'UNI', 'Currency'),
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Currency</h3>'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }

});