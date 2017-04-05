/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filtertop.ComboBox
 */
Ext.define('Uni.grid.filtertop.ReadingQuality', {
    extend: 'Uni.grid.filtertop.ComboBox',
    xtype: 'uni-grid-filtertop-reading-quality',
    multiSelect: true,
    requires: [
        'Uni.store.ReadingQualities'
    ],
    store: 'Uni.store.ReadingQualities',
    displayField: 'name',
    valueField: 'id',
    tpl: Ext.create('Ext.XTemplate',
        '<ul class="x-list-plain">',
        '<tpl for=".">',
        '<li role="option" class="x-boundlist-item">',
        '<div class="x-combo-list-item">',
        '<img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="top: 2px; left: -2px; position: relative;"/>',
        '{name}&nbsp;&nbsp;',
        '<tpl switch="id">',
        '<tpl case="suspects">',
        '<span class="icon-flag5" style="color:red;"></span>',
        '<tpl case="confirmed">',
        '<span class="icon-checkmark" style="color:#686868"></span>',
        '<tpl case="estimates">',
        '<span class="icon-flag5" style="color:#33CC33;"></span>',
        '<tpl case="informatives">',
        '<span class="icon-flag5" style="color:#dedc49;"></span>',
        '<tpl case="edited">',
        '<span class="icon-pencil4" style="color:#686868"></span>',
        '</tpl>',
        '</div>',
        '</li>',
        '</tpl>',
        '</ul>'
    )
});


