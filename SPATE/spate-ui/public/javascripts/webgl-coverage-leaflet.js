/**
 * Created by costantinos on 22/2/2016.
 */
/*
 * Creative Commons Copyright 2013 Ursudio <info@ursudio.com>
 * http://www.ursudio.com/
 * Please attribute Ursudio in any production associated with this javascript plugin.
 */

L.TileLayer.WebGLCoverageMap = L.Class.extend({

    options: {
        size: 30000, // in meters
        opacity: 1,
        gradientTexture: false,
        alphaRange: 1,
        intensityToAlpha:true,
        autoresize: false,
        limit: {min: -100, max: 0},
        legendTitle: "Legend",
        legend: false
    },

    initialize: function (options) {
        this.data = [];
        L.Util.setOptions(this, options);
    },
    getColor: function (d) {

        return d > 0.75 ? 'rgb( 0,' + Math.round(d * 255) + ', 0);' :
            d > 0.5 ? 'rgb(' + Math.round(2 * d * 255) + ',' + Math.round(1.5 * d * 255) + ' , 0 );' :
                d > 0.25 ? 'rgb(' + Math.round((4 * d) * 255) + ',' + Math.round((2 * d) * 255) + ' , ' + Math.round(0.25 * d * 255) + ');' :
                'rgb(' + Math.round((1 - d) * 255) + ' ,0, 0);';

    },
    addLegend: function (map) {
        var info = L.control();
        var parent = this;
        var stats = info.onAdd = function (map) {
            this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
            this.update();
            return this._div;
        };

// method that we will use to update the control based on feature properties passed
        info.update = function () {
            var max = this.options.limit.max;
            var min = this.options.limit.min;
            var minmax = (max - min) / 4;
            var grades = [max, Math.round(min + 3 * minmax), Math.round(min + 2 * minmax), Math.round(min + minmax), min];
            this._div.innerHTML = '<h4>' + this.options.legendTitle + '</h4>';
            var cls = this.hashCode(this.options.legendTitle);
            var divider = max > 0 ? max : min;
            for (var i = 0; i < grades.length; i++) {
                var index = max > 0 ? i : grades.length - 1 - i;
                var color = this.getColor((grades[index] / (divider)));
                this._div.innerHTML +=
                    '<i class="legend-i" style="background:' + color + '"></i> <span class="legend-label">' +
                    grades[i] + (grades[i + 1] ? ' &ndash; ' + grades[i + 1] + '</span><br>' : '+');
            }

        };


        info.addTo(map);

    }, hashCode: function (str) {
        var hash = 0;
        if (str.length == 0) return hash;
        for (var i = 0; i < str.length; i++) {
            var char = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }
    ,
    addLegendToDiv: function (legenddiv) {
        var max = this.options.limit.max;
        var min = this.options.limit.min;
        var minmax = (max - min) / 4;
        var grades = [max, Math.round(min + 3 * minmax), Math.round(min + 2 * minmax), Math.round(min + minmax), min];
        var innerHTML = '<h4>' + this.options.legendTitle + '</h4>';
        var cls = this.hashCode(this.options.legendTitle);
        var divider = max > 0 ? max : min;
        for (var i = 0; i < grades.length; i++) {
            var index = max > 0 ? i : grades.length - 1 - i;
            var color = this.getColor((grades[index] / (divider)));
            innerHTML +=
                '<i class="legend-i" style="background:' + color + '"></i> <span class="legend-label">' +
                grades[i] + (grades[i + 1] ? ' &ndash; ' + grades[i + 1] + '</span><br>' : '+');
        }
        legenddiv.find(".chart-legend_" + cls).remove();
        legenddiv.append('<div class="panel panel-default chart-legend_' + cls + '"> <div class="panel-body">' + innerHTML + "</div></div>");
    },
    onAdd: function (map) {

        this.map = map;
        var mapsize = map.getSize();
        var options = this.options;

        var c = document.createElement("canvas");
        c.id = 'webgl-leaflet';
        c.width = mapsize.x;
        c.height = mapsize.y;
        c.style.opacity = options.opacity;
        c.style.position = 'absolute';

        map.getPanes().overlayPane.appendChild(c);

        this.WebGLCoverageMap = createWebGLCoverageMap({
            canvas: c,
            gradientTexture: options.gradientTexture,
            intensityToAlpha: options.intensityToAlpha,
            alphaRange: [0, options.alphaRange]
        });

        this.canvas = c;

        /* This needs to be fixed somehow. 'moveend' triggers this._plot 3 times for each window resize. */
        map.on("moveend", this._plot, this);

        /* hide layer on zoom, because it doesn't animate zoom */
        map.on("zoomstart", this._hide, this);
        map.on("zoomend", this._show, this);

        if (this.options.autoresize) {
            //a timeout should keep the function from running constantly while resizing
            var timeout;
            var self = this;
            window.onresize = function () {
                window.clearTimeout(timeout);
                timeout = window.setTimeout(function () {
                    self.resize();
                }, 250);
            };
        }

        this._plot();

        if (this.options.legend) {
            if (this.options.legenddiv)
                this.addLegendToDiv(this.options.legenddiv);
            else
                this.addLegend(map);
        }
    },

    onRemove: function (map) {
        map.getPanes().overlayPane.removeChild(this.canvas);
        map.off("moveend", this._plot, this);
        map.off("zoomstart", this._hide, this);
        map.off("zoomend", this._show, this);
    },

    _hide: function () {
        this.canvas.style.display = 'none';
    },

    _show: function () {
        this.canvas.style.display = 'block';
    },

    _clear: function () {

        var heatmap = this.WebGLCoverageMap;
        heatmap.clear();
        heatmap.display();
    },

    _plot: function () {
        this.active = true;
        var map = this.map;
        var heatmap = this.WebGLCoverageMap;
        heatmap.clear();
        L.DomUtil.setPosition(this.canvas, map.latLngToLayerPoint(map.getBounds().getNorthWest()));
        var dataLen = this.data.length;
        if (dataLen) {
            for (var i = 0; i < dataLen; i++) {
                var dataVal = this.data[i],
                    latlng = new L.LatLng(dataVal[0], dataVal[1]),
                    point = map.latLngToContainerPoint(latlng);
                heatmap.addPoint(
                    Math.floor(point.x),
                    Math.floor(point.y),
                    this._scale(latlng),
                    dataVal[2]);
            }
            heatmap.update();
            heatmap.display();
        }
    },

    _scale: function (latlng) {
        // necessary to maintain accurately sized circles
        // to change scale to miles (for example), you will need to convert 40075017 (equatorial circumference of the Earth in metres) to miles
        var lngRadius = (this.options.size / 40075017) * 360 / Math.cos(L.LatLng.DEG_TO_RAD * latlng.lat);
        var latlng2 = new L.LatLng(latlng.lat, latlng.lng - lngRadius);
        var point = this.map.latLngToLayerPoint(latlng);
        var point2 = this.map.latLngToLayerPoint(latlng2);

        return Math.max(Math.round(point.x - point2.x), 1);
    },
    _myscale: function (latlng) {
        //(R) in meters, and bearing (theta) counterclockwise from due east. And for hundereds of meters, plane geometry
        // should be accurate enough.

        var R = this.options.size;

        var theta = 90;
        var dx = R * Math.cos(theta);

        var dy = R * Math.sin(theta);
        //The difference between the constants 110540 and 111320 is due to the earth's oblateness (polar and equatorial circumferences are different).

        var delta_longitude = dx / (111320 * Math.cos(latlng.lat));
        var delta_latitude = dy / 110540;
        var latlng2 = new L.LatLng(latlng.lat - delta_latitude, latlng.lng - delta_longitude);
        var point = this.map.latLngToLayerPoint(latlng);
        var point2 = this.map.latLngToLayerPoint(latlng2);

        return Math.max(Math.round(point.x - point2.x), 1);
    },


    resize: function () {
        //helpful for maps that change sizes
        var mapsize = this.map.getSize();
        this.canvas.width = mapsize.x;
        this.canvas.height = mapsize.y;

        this.WebGLCoverageMap.adjustSize();

        this._plot();
    },

    addDataPoint: function (lat, lon, value) {
        this.data.push([lat, lon, value / 100]);
    },
    addDataPointWithLimit: function (lat, lon, value) {
        var limit = this.options.limit.max;
        var val = ( value / limit) < 0 ? 0 : (value / limit);

        this.data.push([lat, lon, val]);
    },
    addDataReversePointWithLimit: function (lat, lon, value) {
        var limit = this.options.limit.max;
        var val = (1 - value / limit) < 0 ? 0.1 : (1 - value / limit);
        this.data.push([lat, lon, val]);
    },

    setData: function (dataset) {
        // format: [[lat, lon, intensity],...]
        this.data = dataset;
    },
    addData: function (dataset) {
        // format: [[lat, lon, intensity],...]
        dataset.forEach(function (e) {
            this.data.push([e[0], e[1], e[2] / -110]);
        }, this);

    },
    addDataWithLimit: function (dataset) {
        var limit = this.options.limit.min;

        // format: [[lat, lon, intensity],...]
        dataset.forEach(function (e) {
            this.data.push([e[0], e[1], e[2] / limit]);
        }, this);

    },
    clearData: function () {
        this.data = [];
        if (this.options.legend) {
            if (this.options.legenddiv)
                this.addLegendToDiv(this.options.legenddiv);
            else
                this.addLegend(map);
        }
    },

    update: function () {
        if (this.options.legend) {
            if (this.options.legenddiv)
                this.addLegendToDiv(this.options.legenddiv);
            else
                this.addLegend(map);
        }
        this._plot();
    },
    display: function(){
        var heatmap = this.WebGLCoverageMap;
        heatmap.display();
    }
});

L.TileLayer.webglcovaragemap = function (options) {
    return new L.TileLayer.WebGLCoverageMap(options);
};