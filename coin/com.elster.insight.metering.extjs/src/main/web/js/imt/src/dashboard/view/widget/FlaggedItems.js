Ext.define('Imt.dashboard.view.widget.FlaggedItems', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.flagged-items',
    buttonAlign: 'left',
    layout: 'fit',
    title: ' ',
    router: null,
    header: {
        ui: 'small'
    },
    store: null,
    saveModel: null,
    emptyText: '',
    clickToAddText: '',
    clickToRemoveText: '',
    tooltipTpl: '',
    propertyName: '',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'dataview',
                store: me.store,
                itemId: 'items-dataview',
                style: 'max-height: 207px',
                overflowY: 'auto',
                itemSelector: 'a.x-btn.flag-toggle',
                emptyText: me.emptyText,
                style: {
                    margin: '0px !important',
                    left: '5px',
                    top: '-1px'
                },
                tpl: new Ext.XTemplate(
                    '<table style="margin: 0px 0px 0px -3px">',
                    '<tpl for=".">',
                    '<tr id="{name}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" class="device">',
                    '<td width="100%"><a href="{href}">{[Ext.htmlEncode(values.name)]}</a></td>',
                    '<tpl if="this.showButton()">',
                    '<td>',
                    '<a data-qtip="' +
                    me.clickToRemoveText +
                    '" class="flag-toggle x-btn x-btn-plain-small">',
                    '<span style="width: 16px; height: 16px; font-size: 16px" class="x-btn-button"><span class="x-btn-icon-el icon-star-full"></span></span></a>',
                    '</td>',
                    '</tpl>',
                    '</tr>',
                    '</tpl>',
                    '</table>',
                    {
                        showButton: function () {
                            return me.canFlag();
                        }
                    }
                ),

                listeners: {
                    'itemclick': function (view, record, item) {
                        var elm = new Ext.dom.Element(item);
                        var icon = elm.down('.x-btn-icon-el');
                        var pressed = icon.hasCls('icon-star-full');

                        var callback = function (rec, operation) {
                            if (operation && !Ext.isEmpty(operation.response.responseText)) {

                                if (Ext.decode(operation.response.responseText).creationDate) {
                                    var flaggedDate = Ext.decode(operation.response.responseText).creationDate;
                                }
                                else {
                                    var flaggedDate = 0;
                                }

                                var clone = new record.self();
                                var data = record.getWriteData(false, true);
                                clone.set(data);
                                clone.set('flaggedDate', flaggedDate);

                                Ext.get(record.getId()).set({'data-qtip': record.get('tooltipTpl').apply(clone.getData(true))});
                            }
                            icon.toggleCls('icon-star-full');
                            icon.toggleCls('icon-star-empty');
                            elm.set({
                                'data-qtip': pressed ? me.clickToAddText : me.clickToRemoveText
                            });
                        };
                        pressed ? view.flag(record, false, callback) : view.flag(record, true, callback);
                    }
                },

                flag: function (record, favorite, callback) {
                    var favoriteRecord = Ext.create(me.saveModel, {
                        id: record.get(me.propertyName),
                        favorite: favorite,
                        parent: {
                            id: record.get('parent').id,
                            version: record.get('parent').version
                        }
                    });
                    favoriteRecord.save({
                        callback: callback
                    });
                }
            }
        ];

        if (me.showSelectButton) {
            me.bbar = {
                xtype: 'container',
                style: 'margin: 0px 0px 5px 0px',
                itemId: 'docked-usage-point-groups-links-container',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-select',
                        text: Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.selectBtn', 'IMT', 'Select'),
                        style: 'margin-top: 15px; margin-left:4px',
                        href: '#/dashboard/favoriteusagepointgroups'
                    }]
            };

        }
        me.callParent(arguments);
    },

    reload: function () {
        var me = this,
            elm = me.down('#items-dataview'),
            store = elm.getStore();

        me.setTitle('');
        store.load(function () {
            var title = '<h3>'
                + Ext.String.format(me.titleTxt, store.count())
                + '</h3>';
            me.setTitle(title);

            store.each(function (item) {
                item.set('href', me.getHref(item));
                item.set('tooltip', me.tooltipTpl.apply(item.getData(true)));
                item.set('tooltipTpl', me.tooltipTpl);
            });

            elm.refresh();
        });
    }
});