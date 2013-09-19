/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-05-16 14:36:50 (f9be68accb407158ba2b1be2c226a6ce1f649314)
*/
Ext.onReady(function(){var a=Ext.ClassManager,b=Ext.Function.bind(a.get,a);if(Ext.Updater){Ext.Updater.defaults.indicatorText='<div class="loading-indicator">Pemuatan...</div>'}if(Ext.Date){Ext.Date.monthNames=["Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"];Ext.Date.getShortMonthName=function(c){return Ext.Date.monthNames[c].substring(0,3)};Ext.Date.monthNumbers={Jan:0,Feb:1,Mar:2,Apr:3,Mei:4,Jun:5,Jul:6,Agu:7,Sep:8,Okt:9,Nov:10,Des:11};Ext.Date.getMonthNumber=function(c){return Ext.Date.monthNumbers[c.substring(0,1).toUpperCase()+c.substring(1,3).toLowerCase()]};Ext.Date.dayNames=["Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"];Ext.Date.getShortDayName=function(c){return Ext.Date.dayNames[c].substring(0,3)}}if(Ext.MessageBox){Ext.MessageBox.buttonText={ok:"OK",cancel:"Batal",yes:"Ya",no:"Tidak"}}if(Ext.util&&Ext.util.Format){Ext.apply(Ext.util.Format,{thousandSeparator:".",decimalSeparator:",",currencySign:"Rp",dateFormat:"d/m/Y"})}});Ext.define("Ext.locale.id.view.View",{override:"Ext.view.View",emptyText:""});Ext.define("Ext.locale.id.grid.plugin.DragDrop",{override:"Ext.grid.plugin.DragDrop",dragText:"{0} baris terpilih"});Ext.define("Ext.locale.id.tab.Tab",{override:"Ext.tab.Tab",closeText:"Tutup tab ini"});Ext.define("Ext.locale.id.form.field.Base",{override:"Ext.form.field.Base",invalidText:"Isian belum benar"});Ext.define("Ext.locale.id.view.AbstractView",{override:"Ext.view.AbstractView",loadingText:"Pemuatan..."});Ext.define("Ext.locale.id.picker.Date",{override:"Ext.picker.Date",todayText:"Hari ini",minText:"Tanggal ini sebelum batas tanggal minimal",maxText:"Tanggal ini setelah batas tanggal maksimal",disabledDaysText:"",disabledDatesText:"",nextText:"Bulan Berikut (Kontrol+Kanan)",prevText:"Bulan Sebelum (Kontrol+Kiri)",monthYearText:"Pilih bulan (Kontrol+Atas/Bawah untuk pindah tahun)",todayTip:"{0} (Spacebar)",format:"d/m/y",startDay:1});Ext.define("Ext.locale.id.picker.Month",{override:"Ext.picker.Month",okText:"&#160;OK&#160;",cancelText:"Batal"});Ext.define("Ext.locale.id.toolbar.Paging",{override:"Ext.PagingToolbar",beforePageText:"Hal",afterPageText:"dari {0}",firstText:"Hal. Pertama",prevText:"Hal. Sebelum",nextText:"Hal. Berikut",lastText:"Hal. Akhir",refreshText:"Segarkan",displayMsg:"Menampilkan {0} - {1} dari {2}",emptyMsg:"Data tidak ditemukan"});Ext.define("Ext.locale.id.form.field.Text",{override:"Ext.form.field.Text",minLengthText:"Panjang minimal untuk field ini adalah {0}",maxLengthText:"Panjang maksimal untuk field ini adalah {0}",blankText:"Field ini wajib diisi",regexText:"",emptyText:null});Ext.define("Ext.locale.id.form.field.Number",{override:"Ext.form.field.Number",minText:"Nilai minimal untuk field ini adalah {0}",maxText:"Nilai maksimal untuk field ini adalah {0}",nanText:"{0} bukan angka"});Ext.define("Ext.locale.id.form.field.Date",{override:"Ext.form.field.Date",disabledDaysText:"Disfungsi",disabledDatesText:"Disfungsi",minText:"Tanggal dalam field ini harus setelah {0}",maxText:"Tanggal dalam field ini harus sebelum {0}",invalidText:"{0} tanggal salah - Harus dalam format {1}",format:"d/m/y",altFormats:"d/m/Y|d-m-y|d-m-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d"});Ext.define("Ext.locale.id.form.field.ComboBox",{override:"Ext.form.field.ComboBox",valueNotFoundText:undefined},function(){Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig,{loadingText:"Pemuatan..."})});Ext.define("Ext.locale.id.form.field.VTypes",{override:"Ext.form.field.VTypes",emailText:'Field ini harus dalam format email seperti "user@example.com"',urlText:'Field ini harus dalam format URL seperti "http://www.example.com"',alphaText:"Field ini harus terdiri dari huruf dan _",alphanumText:"Field ini haris terdiri dari huruf, angka dan _"});Ext.define("Ext.locale.id.form.field.HtmlEditor",{override:"Ext.form.field.HtmlEditor",createLinkText:"Silakan masukkan URL untuk tautan:"},function(){Ext.apply(Ext.form.field.HtmlEditor.prototype,{buttonTips:{bold:{title:"Tebal (Ctrl+B)",text:"Buat tebal teks terpilih",cls:Ext.baseCSSPrefix+"html-editor-tip"},italic:{title:"Miring (CTRL+I)",text:"Buat miring teks terpilih",cls:Ext.baseCSSPrefix+"html-editor-tip"},underline:{title:"Garisbawah (CTRl+U)",text:"Garisbawahi teks terpilih",cls:Ext.baseCSSPrefix+"html-editor-tip"},increasefontsize:{title:"Perbesar teks",text:"Perbesar ukuran fonta",cls:Ext.baseCSSPrefix+"html-editor-tip"},decreasefontsize:{title:"Perkecil teks",text:"Perkecil ukuran fonta",cls:Ext.baseCSSPrefix+"html-editor-tip"},backcolor:{title:"Sorot Warna Teks",text:"Ubah warna latar teks terpilih",cls:Ext.baseCSSPrefix+"html-editor-tip"},forecolor:{title:"Warna Fonta",text:"Ubah warna teks terpilih",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifyleft:{title:"Rata Kiri",text:"Ratakan teks ke kiri",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifycenter:{title:"Rata Tengah",text:"Ratakan teks ke tengah editor",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifyright:{title:"Rata Kanan",text:"Ratakan teks ke kanan",cls:Ext.baseCSSPrefix+"html-editor-tip"},insertunorderedlist:{title:"Daftar Bulet",text:"Membuat daftar berbasis bulet",cls:Ext.baseCSSPrefix+"html-editor-tip"},insertorderedlist:{title:"Daftar Angka",text:"Membuat daftar berbasis angka",cls:Ext.baseCSSPrefix+"html-editor-tip"},createlink:{title:"Hipertaut",text:"Buat teks terpilih sebagai Hipertaut",cls:Ext.baseCSSPrefix+"html-editor-tip"},sourceedit:{title:"Edit Kode Sumber",text:"Pindah dalam mode kode sumber",cls:Ext.baseCSSPrefix+"html-editor-tip"}}})});Ext.define("Ext.locale.id.grid.header.Container",{override:"Ext.grid.header.Container",sortAscText:"Urut Naik",sortDescText:"Urut Turun",lockText:"Kancing Kolom",unlockText:"Lepas Kunci Kolom",columnsText:"Kolom"});Ext.define("Ext.locale.id.grid.GroupingFeature",{override:"Ext.grid.GroupingFeature",emptyGroupText:"(Kosong)",groupByText:"Kelompokkan Berdasar Field Ini",showGroupsText:"Tampil Dalam Kelompok"});Ext.define("Ext.locale.id.grid.PropertyColumnModel",{override:"Ext.grid.PropertyColumnModel",nameText:"Nama",valueText:"Nilai",dateFormat:"d/m/Y"});Ext.define("Ext.locale.id.window.MessageBox",{override:"Ext.window.MessageBox",buttonText:{ok:"OK",cancel:"Batal",yes:"Ya",no:"Tidak"}});Ext.define("Ext.locale.id.Component",{override:"Ext.Component"});