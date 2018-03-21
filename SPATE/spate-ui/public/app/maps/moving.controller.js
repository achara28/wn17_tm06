(function () {
    'use strict';

    angular
        .module('app')
        .controller('AnalyticsController', AnalyticsController);

    AnalyticsController.$inject = ['AuthenticationService', '$rootScope', '$location', '$scope', '$http', 'sharedProperties', 'rainbowBar'];
    function AnalyticsController(AuthenticationService, $rootScope, $location, $scope, $http, sharedProperties, rainbowBar) {
        var vm = this;
        vm.mapCluster = null;
        vm.authdata = null;
        vm.selection = $location.path();
        vm.CallingPartyIMSIs = ['All'];
        vm.productIDs = ['All'];
        vm.cellTowersIndex = {};
        vm.usersTimeIndex = {};
        vm.data = sharedProperties.getData();
        vm.startdate = vm.data.startdate;
        vm.enddate = vm.data.enddate;
        vm.cdrLayers = [];
        vm.mapCellCluster = null;
        vm.heatmapLayer2g = null;
        vm.heatmapLayer3g = null;
        vm.heatmapLayer4g = null;
        // Base64 encoding service used by AuthenticationService
        var Base64 = AuthenticationService.Base64;

        initController();

        intializeSettings();

        function intializeSettings() {

            $('[type="checkbox"]').bootstrapSwitch({size: "mini"});

            /*
             * Initialize checkboxes
             */


            $('input[name="show-cells"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    loadCells($http);
                }
                else {
                    if (vm.mapCellCluster)
                        vm.mapCellCluster.clearLayers();
                }
                $rootScope.showCells = state;
            });


            $('input[name="show-heatmap2g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    if (!vm.heatmapLayer2g)
                        load2G($http);
                    else
                        vm.map.addLayer(vm.heatmapLayer2g)
                }
                else {
                    if (vm.heatmapLayer2g)
                        vm.map.removeLayer(vm.heatmapLayer2g)
                }
                $rootScope.showHeatmap2g = state;
            });

            $('input[name="show-heatmap3g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    if (!vm.heatmapLayer3g)
                        load3G($http);
                    else
                        vm.map.addLayer(vm.heatmapLayer3g)
                }
                else {
                    if (vm.heatmapLayer3g)
                        vm.map.removeLayer(vm.heatmapLayer3g)
                }
                $rootScope.showHeatmap3g = state;
            });

            $('input[name="show-heatmap4g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    if (!vm.heatmapLayer4g)
                        load4G($http);
                    else
                        vm.map.addLayer(vm.heatmapLayer4g)
                }
                else {
                    if (vm.heatmapLayer4g)
                        vm.map.removeLayer(vm.heatmapLayer4g)
                }
                $rootScope.showHeatmap4g = state;
            });

            $('input[name="show-cells-count"]').on('switchChange.bootstrapSwitch', function (event, state) {

                $rootScope.showCellsCount = state;
                if (vm.mapCellCluster)
                    vm.mapCellCluster.clearLayers();
                loadCells($http);

            });

            $('input[name="show-cells"]').bootstrapSwitch('state', $rootScope.showCells, true);
            $('input[name="show-cells-count"]').bootstrapSwitch('state', $rootScope.showCellsCount, true);
            $('input[name="show-heatmap2g"]').bootstrapSwitch('state', $rootScope.showHeatmap2g, true);
            $('input[name="show-heatmap3g"]').bootstrapSwitch('state', $rootScope.showHeatmap3g, true);
            $('input[name="show-heatmap4g"]').bootstrapSwitch('state', $rootScope.showHeatmap4g, true);

            $('[tdata-toggle="tooltip"]').tooltip({'placement': 'bottom'});
        }

        function load2G($http) {
            var responsePromise = $http.get("/2G", {
                headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                //custom size for this example, and autoresize because map style has a percentage width
                vm.heatmapLayer2g = new L.TileLayer.WebGLHeatMap({size: 5000, autoresize: true, opacity: 0.5});
                vm.heatmapLayer2g.setData(data);
                vm.map.addLayer(vm.heatmapLayer2g);
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("AJAX failed!");
            });
        }

        function load3G($http) {
            var responsePromise = $http.get("/3G", {
                headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                //custom size for this example, and autoresize because map style has a percentage width
                vm.heatmapLayer3g = new L.TileLayer.WebGLHeatMap({size: 5000, autoresize: true, opacity: 0.5});
                vm.heatmapLayer3g.setData(data);
                vm.map.addLayer(vm.heatmapLayer3g);
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("AJAX failed!");
            });
        }

        function load4G($http) {
            var responsePromise = $http.get("/4G", {
                headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                //custom size for this example, and autoresize because map style has a percentage width
                vm.heatmapLayer4g = new L.TileLayer.WebGLHeatMap({size: 5000, autoresize: true, opacity: 0.5});
                vm.heatmapLayer4g.setData(data);
                vm.map.addLayer(vm.heatmapLayer4g);
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
            });
        }


        function initializeSlider(){

            // Instantiate a slider
            vm.downloadSlider = $("#download").slider({
                formatter: function (value) {
                    return value + ' KB';
                }
            });



            var downloadVal;

            $('#download').slider().on('slideStart', function(ev){
                downloadVal = vm.downloadSlider.data().slider.value;
            });

            $('#download').slider().on('slideStop', function(ev){
                var newVal = vm.downloadSlider.data().slider.value;
                if(downloadVal != newVal) {
                    loadMoving($http, vm.startdate, vm.enddate);
                }
            });


            vm.uploadSlider = $("#upload").slider({
                formatter: function (value) {
                    return value + ' KB';
                }
            });

            var upVal;

            $('#upload').slider().on('slideStart', function(ev){
                upVal = vm.uploadSlider.data().slider.value;
            });

            $('#upload').slider().on('slideStop', function(ev){
                var newVal = vm.uploadSlider.data().slider.value;
                if(upVal != newVal) {
                    loadMoving($http, vm.startdate, vm.enddate);
                }
            });
        }


        function initController() {
            $scope.service = sharedProperties;

            vm.map = L.map('map', {
                center: [35.078, 33.385],
                zoom: 9,
                minZoom: 8,
                zoomControl: false,
                contextmenu: true
            });

            //Add settings button

            L.easyButton('fa-list-ul', function (btn, map) {
                $('#settings').modal('toggle', 'center');
                $('.modal-dialog').draggable({handle: '.modal-header'});
            }).addTo(vm.map); // probably just `map`

            vm.map.on('contextmenu', function () {
                vm.map.zoomOut();
            });

            new L.Control.Zoom({position: 'bottomright'}).addTo(vm.map);
            L.control.locate({position: 'bottomleft'}).addTo(vm.map);

            // Creates a red marker with the user icon
            var user = L.AwesomeMarkers.icon({
                icon: 'user',
                markerColor: 'red', prefix: 'fa', iconColor: 'white'
            });

            var self = this;
            vm.mapCluster = new L.MarkerClusterGroup({
                iconCreateFunction: function (cluster) {
                    var markers = cluster.getAllChildMarkers();
                    var count = markers.length;

                    var color = 'red' +
                        '';
                    //if (self.analyticspv)
                    //    color = 'red';


                    var cell = L.AwesomeMarkers.icon({
                        icon: 'none', markerColor: color, iconColor: 'white', html: count
                    });
                    return cell;
                },
                spiderfyDistanceMultiplier: 3,
                zoomToBoundsOnClick: false
            });

            vm.mapCluster.on('clusterdblclick', function (a) {
                a.layer.zoomToBounds();
            });

            var mapTileLayer = L.tileLayer('http://{s}.tiles.mapbox.com/v4/{mapId}/{z}/{x}/{y}.png?access_token={token}', {
                attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery � <a href="http://mapbox.com">Mapbox</a>',
                subdomains: ['a', 'b', 'c', 'd'],
                mapId: 'costantinos.cif3tokoo00mhstly6rbj7q1h',
                detectRetina: true,
                token: 'pk.eyJ1IjoiY29zdGFudGlub3MiLCJhIjoiY2lmM3Rva3dwMDBtMnQ2a2dtZXZsMGN5ZiJ9.ZRipdFAtFyBNU1aev1nwNA'
            }).addTo(vm.map);

            var satTileLayer = L.tileLayer('http://{s}.tiles.mapbox.com/v4/{mapId}/{z}/{x}/{y}.png?access_token={token}', {
                attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery � <a href="http://mapbox.com">Mapbox</a>',
                subdomains: ['a', 'b', 'c', 'd'],
                mapId: 'costantinos.nifc0ceb',
                detectRetina: true,
                token: 'pk.eyJ1IjoiY29zdGFudGlub3MiLCJhIjoiY2lmM3Rva3dwMDBtMnQ2a2dtZXZsMGN5ZiJ9.ZRipdFAtFyBNU1aev1nwNA'
            });

            L.control.layers({
                "Street": mapTileLayer,
                "Satellite": satTileLayer
            }, null, {position: 'bottomright'}).addTo(vm.map);

            var map = vm.map;
            setTimeout(function () {
                map.invalidateSize();
            }, 10);

            vm.map.addLayer(vm.mapCluster);
            loadCells($http);

            if ($rootScope.showHeatmap2g) {
                // vm.heatmapLayer2g.addTo(vm.map);
                load2G($http)

            }

            if ($rootScope.showHeatmap3g) {
                // vm.heatmapLayer3g.addTo(vm.map);
                load3G($http)

            }

            if ($rootScope.showHeatmap3g) {
                // vm.heatmapLayer2g.addTo(vm.map);
                load4G($http)

            }


            $scope.$watch('service.getData()', function (newVal, oldVal) {
                if (newVal.startdate === oldVal.startdate && newVal.enddate === oldVal.enddate) {
                    return;
                }
                vm.startdate = newVal.startdate;
                vm.enddate = newVal.enddate;
                loadMoving($http, vm.startdate, vm.enddate);
            }, true);


            initializeSlider();

        }

        function shadeColor(color, percent) {
            var num = parseInt(color.slice(1), 16), amt = Math.round(2.55 * percent), R = (num >> 16) + amt, G = (num >> 8 & 0x00FF) + amt, B = (num & 0x0000FF) + amt;
            return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 + (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1);
        }

        function loadCells($http) {
            var responsePromise = $http.get("/cellTowers", {headers: {'Authorization': 'Basic ' + vm.authdata}});

            responsePromise.success(function (data, status, headers, config) {
                if ($rootScope.showCells) {
                    loadGUI(data);
                }
                buildCellTowersIndex(data);
                //Set refresh rate for the map
                setInterval(loadMoving($http, vm.startdate, vm.enddate), $rootScope.timeout)
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
            });
        }

        function loadGUI(data) {


            vm.mapCellCluster = new L.MarkerClusterGroup({
                iconCreateFunction: function (cluster) {
                    var markers = cluster.getAllChildMarkers();
                    var count = markers.length;
                    count = count > 1000 ? Math.round(count / 1000) + "k" : count;
                    var color = 'orange';
                    // Creates an orange marker with the cell tower icon
                    var cell = L.AwesomeMarkers.icon({
                        icon: 'wifi',
                        markerColor: 'orange', prefix: 'fa', iconColor: 'white'
                    });

                    if ($rootScope.showCellsCount)
                        cell = L.AwesomeMarkers.icon({
                            icon: 'none',
                            markerColor: color,
                            iconColor: 'white',
                            html: "<div class='awesome'><span>" + count + "</span></div>"
                        });
                    return cell;
                },
                spiderfyDistanceMultiplier: 3,
                zoomToBoundsOnClick: false
            });

            vm.mapCellCluster.on('clusterdblclick', function (a) {
                a.layer.zoomToBounds();
            });

            // Creates an orange marker with the cell tower icon
            var cell = L.AwesomeMarkers.icon({
                icon: 'wifi',
                markerColor: 'orange', prefix: 'fa', iconColor: 'white'
            });
            // and bulk load the cell towers
            var layers = [];
            data.forEach(function (cellTower) {
                /*
                 cells.cellid: "280100027110011"
                 cells.cellname: "R10_001_MESOGI_1"
                 cells.celltype: "2G"
                 cells.id: "10011"
                 cells.lac: "271"
                 cells.latitude: 34.8066112
                 cells.longitude: 32.4471904
                 cells.nename: "PAF_BSC_001"
                 cells.rac: "71"
                 cells.sitename: "R10_001"
                 */
                var marker = L.marker([cellTower["cells.latitude"], cellTower["cells.longitude"]], {icon: cell});
                marker.type = "cellTower";
                marker.cellTower = cellTower;
                layers.push(marker);
                var title = "<p>" +
                    "<span>Cell Id: " + cellTower["cells.cellid"] + "</span><br/>" +
                    "<span>Cell Name: " + cellTower["cells.cellname"] + "</span>" +
                    "</p>";
                var description = "<p>" +
                    "<span>Supports " + cellTower["cells.celltype"] + "</span><br/>" +
                    "<span>" + cellTower["cells.sitename"] + "</span>" +
                    "</p>";

                marker.bindPopup(title + description);
            });
            vm.mapCellCluster.addLayers(layers);

            vm.map.addLayer(vm.mapCellCluster);
        }

        function loadMoving($http, start, end) {

            if (vm.selection !== $location.path()) {
                return;
            }

            rainbowBar.show();

            var startDate = (start + "").replace(/-/g, "");
            var endDate = (end + "").replace(/-/g, "");
            vm.dataToRequest = {"start": startDate, "end": endDate}

            var responsePromise = $http.post("/moved", vm.dataToRequest, {
                timeout: 30000,
                'Authorization': 'Basic ' + vm.authdata,
                params: vm.dataToRequest
            });

            responsePromise.success(function (data, status, headers, config) {

                var self = vm;
                self.mapCluster.removeLayers(self.cdrLayers);
                // and bulk load the cell towers
                self.cdrLayers = [];
                vm.CallingPartyIMSIs = [];
                vm.productIDs = [];
                vm.usersTimeIndex = {};

                buildUsersTimeIndex(data);
                // Creates a red marker with the user icon
                var user = L.AwesomeMarkers.icon({
                    icon: 'user',
                    markerColor: 'red', prefix: 'fa', iconColor: 'white'
                });

                var index = vm.usersTimeIndex;

                Object.keys(index).forEach(function (key) {
                    var detail = index[key][index[key].length - 1];
                    var cellTower = self.cellTowersIndex[detail["cd.callingcellid"]];
                    if (typeof cellTower === 'undefined')
                        return;

                    var marker = L.marker([cellTower["cells.latitude"], cellTower["cells.longitude"]], {icon: user});
                    marker.type = "user";
                    marker.calling = detail["cd.callingpartynumber"];
                    marker.productId = detail["cd.downflux"];
                    marker.subscriberId = detail["cd.elapseduration"]
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    var title = "<p>" +
                        "<span class='fa fa-mobile'> SerialNo: " + detail["cd.serialno"] + "</span><br/>" +
                        "<span class='fa fa-mobile'> CallingPartyIMSI: " + marker.calling + "</span><br/>" +
                        "<span class='fa fa-mobile'> ProductId: " + marker.productId + "</span>" +
                        "</p>";



                    marker.on('click', function () {
                        //reset inner html
                        domelem.innerHTML = title;
                        var inner = document.createElement('div');
                        inner.id = "inner_" + key;
                        domelem.appendChild(inner);

                        var data = [[]];

                        var userarr = index[key];

                        //640x480
                        inner.style.width = "480px";
                        inner.style.height = "480px";

                        var innermap = L.map(inner, {
                            center: [35.078, 33.385],
                            zoom: 9,
                            minZoom: 8,
                        });

                        L.tileLayer('http://{s}.tiles.mapbox.com/v4/{mapId}/{z}/{x}/{y}.png?access_token={token}', {
                            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
                            subdomains: ['a', 'b', 'c', 'd'],
                            mapId: 'canast02.l51d3pfm',
                            detectRetina: true,
                            continuousWorld: true,
                            token: 'pk.eyJ1IjoiY2FuYXN0MDIiLCJhIjoiVDdFYUpvZyJ9.hPg2gsjFhtYku83jeR4mZg'
                        }).addTo(innermap);

                        var latlngs = Array();
                        //You can just keep adding markers
                        for (var i in userarr) {
                            var v = userarr[i];
                            var x = moment(v["cd.mytimestamp"], 'YYYYMMDDhhmmss').format('LLL');
                            cellTower = self.cellTowersIndex[v["cd.callingcellid"]];
                            // Creates a blue marker with the user icon
                            var moving_user = L.AwesomeMarkers.icon({
                                icon: 'user',
                                iconColor: shadeColor("#OO0000", i / userarr.length * 100),
                                prefix: 'fa',
                                markerColor: 'red'
                            });

                            var m = L.marker([cellTower["cells.latitude"], cellTower["cells.longitude"]], {icon: moving_user});
                            var mdomelem = document.createElement('div');
                            mdomelem.id = "mgraph_" + key;

                            //    //reset inner html
                            mdomelem.innerHTML = '';
                            var minner = document.createElement('div');
                            minner.id = "minner_" + key;
                            mdomelem.appendChild(minner);
                            MG.data_graphic({
                                title: "Download & Upload maxspeed",
                                data: [[{'date': new Date(x), 'value':detail["cd.downflux"] / detail["cd.elapseduration"]}], [{
                                    'date': new Date(x),
                                    'value': detail["cd.upflux"] / detail["cd.elapseduration"]
                                }]],
                                linked: true,
                                width: 300,
                                height: 200,
                                right: 40,
                                area: true,
                                show_tooltips: false,
                                left: 80,
                                y_label: "Bps",
                                legend: ['Download', 'Upload'],
                                target: minner,
                                custom_line_color_map: [3, 2]
                            });

                            //Get latlng from first marker
                            latlngs.push(m.getLatLng());
                            m.addTo(innermap);
                            m.bindPopup(mdomelem);
                        }


                        //From documentation http://leafletjs.com/reference.html#polyline
                        // create a red polyline from an arrays of LatLng points
                        var polyline = L.polyline(latlngs, {
                            color: 'red',
                            weight: 3,
                            opacity: 0.5,
                            smoothFactor: 1
                        });

                        polyline.addTo(innermap);
                        marker.on('popupopen', function () {
                            innermap.invalidateSize();
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                rainbowBar.hide();
            });
        }

        function buildUsersTimeIndex(users) {
            var self = vm;
            //reset index
            self.usersTimeIndex = {};
            users.forEach(function (user) {
                var dmin = vm.downloadSlider.data().slider.value[0];
                var dmax = vm.downloadSlider.data().slider.value[1];
                var umin = vm.uploadSlider.data().slider.value[0];
                var umax = vm.uploadSlider.data().slider.value[1];
                var downFlux = user["cd.downflux"];
                var upFlux = user["cd.upflux"];
                var elapseduration = user["cd.elapseduration"];
                var callingpartynumber = user["cd.callingpartynumber"];
                var productid = user["cd.productid"];
                var download = downFlux / elapseduration / 1024;
                var upload = upFlux / elapseduration / 1024;
                if (dmin <= download && download <= dmax && umin <= upload && upload <= umax) {
                    if (!(callingpartynumber in self.usersTimeIndex)) {
                        self.usersTimeIndex[callingpartynumber] = [];
                        self.CallingPartyIMSIs.push(callingpartynumber);
                        if (!(productid in self.productIDs)) {
                            self.productIDs.push(productid)
                        }
                    }
                    self.usersTimeIndex[callingpartynumber].push(user);
                }
            });
            $scope.callingPartyIMSIs = self.CallingPartyIMSIs;
            $scope.productIDs = self.productIDs;
        }

        function buildCellTowersIndex(cellTowers) {
            //reset index
            vm.cellTowersIndex = {};
            cellTowers.forEach(function (cellTower) {
                vm.cellTowersIndex[cellTower["cells.cellid"]] = cellTower;
            });
        }

        $scope.subscriberDetails = function (calling) {

            $("#collapseExample").collapse('toggle');

            var self = vm;
            var proArr = [];
            var markers = self.cdrLayers;
            for (var i in markers) {
                var markerID = markers[i].calling;
                if (markerID == calling || calling === "All") {
                    proArr.push(markers[i])
                }
            }

            self.mapCluster.removeLayers(self.cdrLayers);
            self.mapCluster.addLayers(proArr);
        };

        $scope.productDetails = function (productId) {

            $("#collapseExample").collapse('toggle');

            var self = vm;
            var proArr = [];
            var markers = self.cdrLayers;
            for (var i in markers) {
                var markerID = markers[i].productId;
                if (markerID == productId || productId === "All") {
                    proArr.push(markers[i])
                }

            }

            self.mapCluster.removeLayers(self.cdrLayers);
            self.mapCluster.addLayers(proArr);
        }

        $('a.export').click(function (ev) {
            ev.preventDefault();
            var html = '<pre>' + JSON.stringify(vm.usersTimeIndex) + '</pre>';
            $('#export .modal-body').html(html);
            $('#export').modal('show', {backdrop: 'static'});
        });

        $('.close').click(function () {
            $scope.subscriberDetails('All');
            $scope.productDetails('All')
        });

    }

})();