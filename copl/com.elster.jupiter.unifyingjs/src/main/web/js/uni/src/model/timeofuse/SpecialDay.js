/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.timeofuse.SpecialDay
 */
Ext.define('Uni.model.timeofuse.SpecialDay', {
  extend: 'Ext.data.Model',
  fields: [
    {name: 'id', type: 'number'},
    {name: 'day', type: 'number'},
    {name: 'month', type: 'string'},
    {name: 'year', type: 'number'},
    {
      name: 'date',
      type: 'date',
      persist: false,
      mapping: function(record) {
        var date = new Date(record.day + ' ' + record.month);
        record.year && date.setFullYear(record.year);

        return date;
      }
    },
    {
      name: 'dayTypeName',
      type: 'string',
      persist: false,
      mapping: function(record) {
        return record.dayType && record.dayType.name;
      }
    }
  ],
});