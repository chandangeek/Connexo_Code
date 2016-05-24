Ext.define('Imt.usagepointmanagement.view.UsagePointMetrologyConfig', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-metrology-config',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 120
    },

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'up-metrology-config-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                renderer: function (value) {
                    var result = '',
                        record = me.getRecord(),
                        canViewMetrologyConfig,
                        activationTime;

                    if (record) {
                        canViewMetrologyConfig = Imt.privileges.MetrologyConfig.canView();
                        activationTime = record.get('activationTime');
                        if (value) {
                            if (canViewMetrologyConfig) {
                                result += '<a href="'
                                    + me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.getId()})
                                    + '">';
                            }
                            result += value;
                            if (canViewMetrologyConfig) {
                                result += '</a>';
                            }
                        }
                        if (activationTime) {
                            result += '<br><span style="font-size: 90%">'
                                + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                + '</span>';
                        }
                    }

                    return result || '-';
                }
            },
            {
                itemId: 'up-metrology-config-empty',
                fieldLabel: ' ',
                hidden: true,
                htmlEncode: false,
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/definemetrology').buildUrl({},{fromLandingPage: true});
                    return Uni.I18n.translate('general.label.defineConfiguration', 'IMT', '<a href="{0}">Define configuration</a>',url);
                },
                listeners: {
                    beforerender: function() {
                        if (!me.getRecord().get('name') && Imt.privileges.UsagePoint.canAdministrate()) {
                            this.show();
                        }
                    }
                }
            },
            {
                itemId: 'up-metrology-config-purposes',
                name: 'purposes',
                xtype: 'fieldcontainer',
                listeners: {
                    afterrender: function() {
                        var fieldcontainer = this,
                            first = true,
                            purposes = me.getRecord().get('purposes');

                        if (!Ext.isEmpty(purposes)) {
                            Ext.Array.each(purposes, function(purpose){
                                purpose.active && fieldcontainer.add({
                                    xtype: 'displayfield',
                                    labelWidth: 120,
                                    margin: first ? 0 : '-13 0 0 0',
                                    fieldLabel: first ? Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes') : '&nbsp;',
                                    value: purpose.name,
                                    renderer: function(value){
                                        var icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon '
                                            + (purpose.status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle2')
                                            + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                            + purpose.status.name
                                            + '"></i>',
                                            url = me.router.getRoute('usagepoints/view/purpose').buildUrl({purposeId: purpose.id}),
                                            link = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                        return link + icon;
                                    }
                                });
                                first = false;
                            });
                        } else {
                            fieldcontainer.add({
                                xtype: 'displayfield',
                                labelWidth: 120,
                                fieldLabel: first ? Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes') : '&nbsp;',
                                value: '-'
                            })
                        }
                    }
                }
            },
            {
                itemId: 'up-metrology-config-meters',
                name: 'meters',
                fieldLabel: Uni.I18n.translate('general.meters', 'IMT', 'Meters'),
                renderer: function (value) {
                    var result = '-';

                    if (!Ext.isEmpty(value)) {
                        // will be implemented in scope of another story
                    }

                    return result;
                }
            },
            {
                itemId: 'up-metrology-config-meters-empty',
                fieldLabel: ' ',
                hidden: true,
                htmlEncode: false,
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/definemetrology').buildUrl({},{fromLandingPage: true}); //to meters
                    return Uni.I18n.translate('general.label.setMeters', 'IMT', '<a href="{0}">Set meters</a>',url);
                },
                listeners: {
                    beforerender: function() {
                        if (!me.getRecord().get('name') && Imt.privileges.UsagePoint.canAdministrate()) { //to meters
                            this.show();
                        }
                    }
                }
            },
        ];

        me.bbar = [
            {
                itemId: 'up-metrology-config-more-details-link',
                ui: 'link',                
                text: Uni.I18n.translate('general.moreDetails', 'IMT', 'More details'),
                href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});