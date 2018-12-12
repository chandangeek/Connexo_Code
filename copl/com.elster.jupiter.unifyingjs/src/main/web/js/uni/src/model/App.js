/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.App
 */
Ext.define('Uni.model.App', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        {
            name: 'url',
            convert: function (value, record) {
                if (value.indexOf('#') === -1 && value.indexOf('http') === -1) {
                    value += '#';
                }
                return value;
            }
        },
        'icon',
        {
            name: 'isActive',
            persist: false,
            convert: function (value, record) {
                var href = window.location.href,
                    pathname = window.location.pathname,
                    fullPath = pathname + window.location.hash;

                return href.indexOf(record.data.url, 0) === 0
                    || fullPath.indexOf(record.data.url, 0) === 0;
            }
        },
        'isExternal',
        {
            name: 'is3External',
            persist: false,
            convert: function (value, record) {
                var url = record.get('url');
                return (url.indexOf('http') === 0);
            }
        }
    ]
});