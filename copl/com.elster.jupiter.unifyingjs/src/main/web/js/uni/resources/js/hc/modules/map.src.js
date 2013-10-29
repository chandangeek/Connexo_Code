/**
 * @license Map plugin v0.1 for Highcharts
 *
 * (c) 2011-2013 Torstein Hønsi
 *
 * License: www.highcharts.com/license
 */

/* 
 * See www.highcharts.com/studies/world-map.htm for use case.
 *
 * To do:
 * - Optimize long variable names and alias adapter methods and Highcharts namespace variables
 * - Zoom and pan GUI
 */
(function (Highcharts) {
	var UNDEFINED,
		Axis = Highcharts.Axis,
		each = Highcharts.each,
		extend = Highcharts.extend,
		merge = Highcharts.merge,
		pick = Highcharts.pick,
		numberFormat = Highcharts.numberFormat,
		plotOptions = Highcharts.getOptions().plotOptions,
		Color = Highcharts.Color,
		noop = function () {};
	
	/**
	 * Utility for reading SVG paths directly.
	 * 
	 * @todo This is moved to the Data plugin. Make sure it is deleted here.
	 */
	Highcharts.pathToArray = function (path) {
		var i;

		// Move letters apart
		path = path.replace(/([A-Za-z])/g, ' $1 ');
		// Trim
		path = path.replace(/^\s*/, "").replace(/\s*$/, "");
		
		// Split on spaces and commas
		path = path.split(/[ ,]+/);
		
		for (i = 0; i < path.length; i++) {
			if (!/[a-zA-Z]/.test(path[i])) {
				path[i] = parseFloat(path[i]);
			}
		}
		return path;
	};
	
	/**
	 * Extend the Axis object with methods specific to maps
	 */
	Highcharts.wrap(Axis.prototype, 'init', function (proceed, chart, userOptions) {
		
		if (chart.options.chart.type === 'map') {
			extend(this, {
				
				/**
				 * Override to use the extreme coordinates from the SVG shape, not the
				 * data values
				 */
				getSeriesExtremes: function () {
					var isXAxis = this.isXAxis,
						dataMin = Number.MAX_VALUE,
						dataMax = Number.MIN_VALUE;
					each(this.series, function (series) {
						dataMin = Math.min(dataMin, series[isXAxis ? 'minX' : 'minY']);
						dataMax = Math.max(dataMax, series[isXAxis ? 'maxX' : 'maxY']);
					});
					this.dataMin = dataMin;
					this.dataMax = dataMax;
				},
				
				/**
				 * Override axis translation to make sure the aspect ratio is always kept
				 */
				setAxisTranslation: function () {
					var chart = this.chart,
						mapRatio,
						plotRatio = chart.plotWidth / chart.plotHeight,
						isXAxis = this.isXAxis,
						adjustedAxisLength,
						xAxis = chart.xAxis[0],
						padAxis;
					
					// Run the parent method
					Axis.prototype.setAxisTranslation.call(this);
					
					// On Y axis, handle both
					if (!isXAxis && xAxis.transA !== UNDEFINED) {
						
						// Use the same translation for both axes
						this.transA = xAxis.transA = Math.min(this.transA, xAxis.transA);
						
						mapRatio = (xAxis.max - xAxis.min) / (this.max - this.min);
						
						// What axis to pad to put the map in the middle
						padAxis = mapRatio > plotRatio ? this : xAxis;
						
						// Pad it
						adjustedAxisLength = (padAxis.max - padAxis.min) * padAxis.transA;
						padAxis.minPixelPadding = (padAxis.len - adjustedAxisLength) / 2;
					}
					
				}
			});
		}	
		
		return proceed.call(this, chart, userOptions);
	});
	
	/**
	 * Extend the default options with map options
	 */
	plotOptions.map = merge(
		plotOptions.scatter, 
		{
			animation: false, // makes the complex shapes slow
			minOpacity: 0.2,
			nullColor: '#F8F8F8',
			borderColor: 'silver',
			borderWidth: 1,
			marker: null,
			stickyTracking: false,
			tooltip: {
				followPointer: true,
				headerFormat: '<span style="font-size:10px">{point.key}</span><br/>',
				pointFormat: '{series.name}: {point.y}<br/>'
			}
		}
	);
	
	/**
	 * Add the series type
	 */
	Highcharts.seriesTypes.map = Highcharts.extendClass(Highcharts.seriesTypes.scatter, {
		type: 'map',
		pointAttrToOptions: { // mapping between SVG attributes and the corresponding options
			stroke: 'borderColor',
			'stroke-width': 'borderWidth',
			fill: 'color'
		},
		colorKey: 'y',
		trackerGroups: ['group', 'markerGroup'],
		getSymbol: noop,
		getExtremesFromAll: true,
		init: function (chart) {
			var series = this,
				valueDecimals = chart.options.legend.valueDecimals,
				legendItems = [],
				name,
				from,
				to,
				fromLabel,
				toLabel,
				colorRange,
				gradientColor,
				grad,
				tmpLabel,
				horizontal = chart.options.legend.layout === 'horizontal';

			
			Highcharts.Series.prototype.init.apply(this, arguments);
			colorRange = series.options.colorRange;

			if (series.options.valueRanges) {
				each(series.options.valueRanges, function (range) {
					from = range.from;
					to = range.to;
					
					// Assemble the default name. This can be overridden by legend.options.labelFormatter
					name = '';
					if (from === UNDEFINED) {
						name = '< ';
					} else if (to === UNDEFINED) {
						name = '> ';
					}
					if (from !== UNDEFINED) {
						name += numberFormat(from, valueDecimals);
					}
					if (from !== UNDEFINED && to !== UNDEFINED) {
						name += ' - ';
					}
					if (to !== UNDEFINED) {
						name += numberFormat(to, valueDecimals);
					}
					
					// Add a mock object to the legend items
					legendItems.push(Highcharts.extend({
						chart: series.chart,
						name: name,
						options: {},
						drawLegendSymbol: Highcharts.seriesTypes.area.prototype.drawLegendSymbol,
						visible: true,
						setState: function () {},
						setVisible: function () {}
					}, range));
				});
				series.legendItems = legendItems;

			} else if (colorRange) {

				from = colorRange.from;
				to = colorRange.to;
				fromLabel = colorRange.fromLabel;
				toLabel = colorRange.toLabel;

				// Flips linearGradient variables and label text.
				grad = horizontal ? [0, 0, 1, 0] : [0, 1, 0, 0]; 
				if (!horizontal) {
					tmpLabel = fromLabel;
					fromLabel = toLabel;
					toLabel = tmpLabel;
				} 

				// Creates color gradient.
				gradientColor = {
					linearGradient: { x1: grad[0], y1: grad[1], x2: grad[2], y2: grad[3] },
					stops: 
					[
						[0, from],
						[1, to]
					]
				};

				// Add a mock object to the legend items.
				legendItems = [{
					chart: series.chart,
					options: {},
					fromLabel: fromLabel,
					toLabel: toLabel,
					color: gradientColor,
					drawLegendSymbol: this.drawLegendSymbol,
					visible: true,
					setState: function () {},
					setVisible: function () {}
				}];

				series.legendItems = legendItems;
			}
		},

		/**
		 * Gets the series' symbol in the legend and extended legend with more information.
		 * 
		 * @param {Object} legend The legend object
		 * @param {Object} item The series (this) or point
		 */
		drawLegendSymbol: function (legend, item) {
			
			var spacing = legend.options.symbolPadding,
				padding = pick(legend.options.padding, 8),
				positionY,
				positionX,
				gradientSize = this.chart.renderer.fontMetrics(legend.options.itemStyle.fontSize).h,
				horizontal = legend.options.layout === 'horizontal',
				box1,
				box2,
				box3,
				rectangleLength = pick(legend.options.rectangleLength, 200);

			// Set local variables based on option.
			if (horizontal) {
				positionY = -(spacing / 2);
				positionX = 0;
			} else {
				positionY = -rectangleLength + legend.baseline - (spacing / 2);
				positionX = padding + gradientSize;
			}

			// Creates the from text.
			item.fromText = this.chart.renderer.text(
					item.fromLabel,	// Text.
					positionX,		// Lower left x.
					positionY		// Lower left y.
				).attr({
					zIndex: 2
				}).add(item.legendGroup);
			box1 = item.fromText.getBBox();

			// Creates legend symbol.
			// Ternary changes variables based on option.
			item.legendSymbol = this.chart.renderer.rect(
				horizontal ? box1.x + box1.width + spacing : box1.x - gradientSize - spacing,		// Upper left x.
				box1.y,																				// Upper left y.
				horizontal ? rectangleLength : gradientSize,											// Width.
				horizontal ? gradientSize : rectangleLength,										// Height.
				2																					// Corner radius.
			).attr({
				zIndex: 1
			}).add(item.legendGroup);
			box2 = item.legendSymbol.getBBox();

			// Creates the to text.
			// Vertical coordinate changed based on option.
			item.toText = this.chart.renderer.text(
					item.toLabel,
					box2.x + box2.width + spacing,
					horizontal ? positionY : box2.y + box2.height - spacing
				).attr({
					zIndex: 2
				}).add(item.legendGroup);
			box3 = item.toText.getBBox();

			// Changes legend box settings based on option.
			if (horizontal) {
				legend.offsetWidth = box1.width + box2.width + box3.width + (spacing * 2) + padding;
				legend.itemY = gradientSize + padding;
			} else {
				legend.offsetWidth = Math.max(box1.width, box3.width) + (spacing) + box2.width + padding;
				legend.itemY = box2.height + padding;
				legend.itemX = spacing;
			}
		},

		/**
		 * Get the bounding box of all paths in the map combined.
		 */
		getBox: function () {
			var chart = this.chart,
				maxX = -Math.pow(2, 31), 
				minX =  Math.pow(2, 31) - 1, 
				maxY = -Math.pow(2, 31), 
				minY =  Math.pow(2, 31) - 1,
				xyRatio,
				ratioCorrection,
				plotWidth = chart.plotWidth, 
				plotHeight = chart.plotHeight,
				pad;
			
			
			// Find the bounding box
			each(this.options.data, function (point) {
				var path = point.path,
					i = path.length,
					even = false; // while loop reads from the end
					
				while (i--) {
					if (typeof path[i] === 'number') {
						if (even) { // even = x
							maxX = Math.max(maxX, path[i]);
							minX = Math.min(minX, path[i]);
						} else { // odd = Y
							maxY = Math.max(maxY, path[i]);
							minY = Math.min(minY, path[i]);
						}
						even = !even;
					}
				}
			});
			this.minY = minY;
			this.maxY = maxY;
			this.minX = minX;
			this.maxX = maxX;
			
		},
		
		
		
		/**
		 * Translate the path so that it automatically fits into the plot area box
		 * @param {Object} path
		 */
		translatePath: function (path) {
			
			var series = this,
				chart = series.chart,
				even = false, // while loop reads from the end
				xAxis = series.xAxis,
				yAxis = series.yAxis;
				
			// Preserve the original
			path = [].concat(path);
				
			// Do the translation
			i = path.length;
			while (i--) {
				if (typeof path[i] === 'number') {
					if (even) { // even = x
						path[i] = Math.round(xAxis.translate(path[i]));
					} else { // odd = Y
						path[i] = Math.round(yAxis.len - yAxis.translate(path[i]));
					}
					even = !even;
				}
			}
			return path;
		},
		
		setData: function () {
			Highcharts.Series.prototype.setData.apply(this, arguments);
			this.getBox();
		},
		
		/**
		 * Add the path option for data points. Find the max value for color calculation.
		 */
		translate: function () {
			var series = this,
				options = series.options,
				dataMin = Number.MAX_VALUE,
				dataMax = Number.MIN_VALUE,
				opacity,
				minOpacity = options.minOpacity,
				path,
				color;
	
			series.generatePoints();
	
			each(series.data, function (point) {
				
				point.shapeType = 'path';
				point.shapeArgs = {
					d: series.translatePath(point.path)
				};
				
				// TODO: do point colors in drawPoints instead of point.init
				if (typeof point.y === 'number') {
					if (point.y > dataMax) {
						dataMax = point.y;
					} else if (point.y < dataMin) {
						dataMin = point.y;
					}
				}
			});
			
			series.translateColors(dataMin, dataMax);
		},
		
		/**
		 * In choropleth maps, the color is a result of the value, so this needs translation tood
		 */
		translateColors: function (dataMin, dataMax) {
			
			var seriesOptions = this.options,
				valueRanges = seriesOptions.valueRanges,
				colorRange = seriesOptions.colorRange,
				colorKey = this.colorKey;
			
			each(this.data, function (point) {
				var value = point[colorKey],
					rgba = [],
					range,
					from,
					to,
					i,
					pos;

				if (valueRanges) {
					i = valueRanges.length;
					while (i--) {
						range = valueRanges[i];
						from = range.from;
						to = range.to;
						if ((from === UNDEFINED || value >= from) && (to === UNDEFINED || value <= to)) {
							point.options.color = range.color;
							break;
						}
							
					}
				} else if (colorRange && value !== undefined) {
					from = Color(colorRange.from);
					to = Color(colorRange.to);
					pos = (dataMax - value) / (dataMax - dataMin);
					i = 4;
					while (i--) {
						rgba[i] = Math.round(
							to.rgba[i] + (from.rgba[i] - to.rgba[i]) * pos
						);
					}
					point.options.color = 'rgba(' + rgba.join(',') + ')';
				}
			});
		},
		
		drawGraph: noop,
		
		/**
		 * We need the points' bounding boxes in order to draw the data labels, so 
		 * we skip it now and call if from drawPoints instead.
		 */
		drawDataLabels: noop,
		
		/** 
		 * Use the drawPoints method of column, that is able to handle simple shapeArgs.
		 * Extend it by assigning the tooltip position.
		 */
		drawPoints: function () {
			var series = this,
				chart = series.chart,
				saturation,
				bBox,
				colorKey = series.colorKey;
			
			// Make points pass test in drawing
			each(series.data, function (point) {
				point.plotY = 1; // pass null test in column.drawPoints
				if (point[colorKey] === null) {
					point[colorKey] = 0;
					point.isNull = true;
				}
			});
			
			// Draw them
			Highcharts.seriesTypes.column.prototype.drawPoints.apply(series);
			
			each(series.data, function (point) {
				
				bBox = point.graphic.getBBox();
				// for tooltip
				point.tooltipPos = [
					bBox.x + bBox.width / 2,
					bBox.y + bBox.height / 2
				];
				// for data labels
				point.plotX = point.tooltipPos[0];
				point.plotY = point.tooltipPos[1]; 
				
				// Reset escaped null points
				if (point.isNull) {
					point[colorKey] = null;
				}
			});

			// Now draw the data labels
			Highcharts.Series.prototype.drawDataLabels.call(series);
			
		}
	});
	
	/**
	 * A wrapper for Chart with all the default values for a Map
	 */
	Highcharts.Map = function (options, callback) {
		
		var hiddenAxis = {
			endOnTick: false,
			gridLineWidth: 0,
			labels: {
				enabled: false
			},
			lineWidth: 0,
			minPadding: 0,
			maxPadding: 0,
			startOnTick: false,
			tickWidth: 0,
			title: null
		};
		
		// Don't merge the data
		seriesOptions = options.series;
		options.series = null;
		
		options = merge({
			chart: {
				type: 'map'
			},
			xAxis: hiddenAxis,
			yAxis: merge(hiddenAxis, { reversed: true })	
		},
		options, // user's options
	
		{ // forced options
			chart: {
				inverted: false
			}
		});
	
		options.series = seriesOptions;
	
	
		return new Highcharts.Chart(options, callback);
	};
}(Highcharts));
