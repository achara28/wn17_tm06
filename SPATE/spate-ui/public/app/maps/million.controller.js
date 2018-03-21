(function () {
    'use strict';

    angular
        .module('app')
        .controller('MillionController', MillionController);

    MillionController.$inject = ['AuthenticationService', '$rootScope', '$timeout', '$scope', '$http', 'sharedProperties', 'rainbowBar'];
    function MillionController(AuthenticationService, $rootScope, $timeout, $scope, $http, sharedProperties, rainbowBar) {
        var vm = this;
        vm.mapCluster = null;
        vm.user = null;
        vm.selection = "location";
        vm.CallingPartyIMSIs = [];
        vm.productIDs = [];
        vm.cellTowersIndex = {};
        vm.usersTimeIndex = {};
        vm.startdate = sharedProperties.startdate;
        vm.enddate = sharedProperties.startdate;
        vm.cdrLayers = [];
        // Base64 encoding service used by AuthenticationService
        var Base64 = AuthenticationService.Base64;

        initController();


        function initController() {
            $scope.service = sharedProperties;
            vm.user = $rootScope.globals.currentUser.username;
            vm.pass = $rootScope.globals.currentUser.password;

            vm.map = L.map('map', {
                center: [35.078, 33.385],
                zoomControl: false
            });
            vm.map.setView([22.295006, 78.945313], 3);

            new L.Control.Zoom({position: 'topright'}).addTo(vm.map);
            L.control.locate({position: 'topright'}).addTo(vm.map);

            var self = this;
            vm.mapCluster = new L.MarkerClusterGroup({
                iconCreateFunction: function (cluster) {
                    var markers = cluster.getAllChildMarkers();
                    var count = markers.length;

                    var color = 'green';
                    var markerCount = 0;
                    markers.forEach(function (m) {
                        markerCount = markerCount + m.count;
                    });


                    var cell = L.AwesomeMarkers.icon({
                        icon: 'none', markerColor: color, iconColor: 'white', html: markerCount
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
            }, null).addTo(vm.map);

            var map = vm.map;
            setTimeout(function () {
                map.invalidateSize();
            }, 10);

            vm.map.addLayer(vm.mapCluster);
            //  loadCells($http);

            //
            var heatTileLayer = L.tileLayer('http://localhost:8080/MTN/{z}/{x}/{y}.png', {
                attribution: 'Map data &copy; MTN'
            }).setZIndex(7)


            heatTileLayer.addTo(vm.map)

            $scope.$watch('service.getData()', function (newVal) {
                vm.start = newVal.startdate;
                vm.end = newVal.enddate;
                //  loadMoving($http);
            }, true);

            vm.map.invalidateSize();
        }

        function makePoints(aggs) {

            var markerList = [];
            var heatList = [];
            aggs.forEach(function (agg, index) {
                var center = geohash.decode (agg.key);//elastic return a geohas so need to change it into lat/lon
                // Creates a blue marker with the user icon
                var myIcon = L.AwesomeMarkers.icon({
                    icon: 'user',
                    markerColor: 'blue', prefix: 'fa', iconColor: 'white'
                });

                var marker = L.marker(new L.LatLng(center.latitude, center.longitude), {icon: myIcon});


                marker.count = agg.doc_count;
                marker.sentiment = agg.sentiment_avg.value;
                marker.bindPopup('' + agg.doc_count);
                markerList.push(marker);
                heatList.push([center.latitude, center.longitude, agg.doc_count]);
            });

            L.heatLayer(heatList).addTo(vm.map);
            vm.mapCluster.addLayers(markerList);
        }

        function shadeColor(color, percent) {
            var num = parseInt(color.slice(1), 16), amt = Math.round(2.55 * percent), R = (num >> 16) + amt, G = (num >> 8 & 0x00FF) + amt, B = (num & 0x0000FF) + amt;
            return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 + (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1);
        }

        function loadCells($http) {
            var authdata = Base64.encode(vm.user + ':' + vm.pass);
            var responsePromise = $http.get("/cellTowers", {headers: {'Authorization': 'Basic ' + authdata}});

            responsePromise.success(function (data, status, headers, config) {

                buildCellTowersIndex(data.cellTowers);
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("AJAX failed!");
            });
        }


        function loadMoving($http) {

            rainbowBar.show();

            var authdata = Base64.encode(vm.user + ':' + vm.pass);
            // if (typeof start !== 'undefined' || typeof end !== 'undefined') {
            var startDate = (vm.start + "").replace(/-/g, "");
            var endDate = (vm.end + "").replace(/-/g, "");
            vm.dataToRequest = {"start": startDate, "end": endDate}
            // }

            var responsePromise = $http.post("/cdr", vm.dataToRequest, {
                'Authorization': 'Basic ' + authdata,
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
                // Creates a blue marker with the user icon
                var user = L.AwesomeMarkers.icon({
                    icon: 'user',
                    markerColor: 'blue', prefix: 'fa', iconColor: 'white'
                });

                var index = vm.usersTimeIndex;

                Object.keys(index).forEach(function (key) {
                    var detail = index[key][index[key].length - 1];
                    var cellTower = self.cellTowersIndex[detail.cellId]


                    var marker = L.marker([cellTower.location[0], cellTower.location[1]], {icon: user});
                    marker.type = "user";
                    marker.calling = detail.calling;
                    marker.productId = detail.productId;
                    marker.subscriberId = detail.subscriberId;
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    var title = "<p>" +
                        "<span class='fa fa-mobile'> SerialNo: " + detail.serialNo + "</span><br/>" +
                        "<span class='fa fa-mobile'> CallingPartyIMSI: " + detail.calling + "</span><br/>" +
                        "<span class='fa fa-mobile'> ProductId: " + detail.productId + "</span>" +
                        "</p>";


                    marker.on('add', function () {
                        //reset inner html
                        domelem.innerHTML = title;
                        var inner = document.createElement('div');
                        inner.id = "inner_" + key;
                        domelem.appendChild(inner);

                        var data = [[]];

                        var userarr = index[key];

                        if (userarr.length > 1 && marker.type === "muser") {
                            //640x480
                            inner.style.width = "640px";
                            inner.style.height = "480px";

                            var innermap = L.map(inner, {
                                center: [35.078, 33.385],
                                zoom: 9,
                                minZoom: 8,
                            });

                            L.tileLayer('https://{s}.tiles.mapbox.com/v4/{mapId}/{z}/{x}/{y}.png?access_token={token}', {
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
                                var x = moment(v.timestamp, 'YYYYMMDDhhmmss').format('LLL');
                                cellTower = self.cellTowersIndex[v.cellId];
                                // Creates a blue marker with the user icon
                                var moving_user = L.AwesomeMarkers.icon({
                                    icon: 'user',
                                    iconColor: self.shadeColor("#OO0000", i / userarr.length * 100),
                                    prefix: 'fa',
                                    markerColor: 'red'
                                });

                                var m = L.marker([cellTower.location[0], cellTower.location[1]], {icon: moving_user});
                                var mdomelem = document.createElement('div');
                                mdomelem.id = "mgraph_" + key;

                                //    //reset inner html
                                mdomelem.innerHTML = '';
                                var minner = document.createElement('div');
                                minner.id = "minner_" + key;
                                mdomelem.appendChild(minner);
                                MG.data_graphic({
                                    title: "Download & Upload maxspeed",
                                    data: [[{'date': new Date(x), 'value': v.downFlux / v.duration}], [{
                                        'date': new Date(x),
                                        'value': v.upFlux / v.duration
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
                            marker.on('click', function () {
                                innermap.invalidateSize();
                            });
                        } else {

                            var data1 = [];
                            var data2 = [];
                            //Iterate through table and put the data on the graph
                            for (var i in userarr) {
                                var v = userarr[i];
                                var x = moment(v.timestamp, 'YYYYMMDDhhmmss').format('LLL');
                                data1.push({'date': new Date(x), 'value': detail.downFlux / detail.duration});
                                data2.push({'date': new Date(x), 'value': detail.upFlux / detail.duration});
                            }

                            data = [data1, data2];

                            MG.data_graphic({
                                title: "Download & Upload maxspeed",
                                data: data,
                                linked: true,
                                width: 600,
                                height: 240,
                                right: 80,
                                area: true,
                                show_tooltips: false,
                                left: 80,
                                bottom: 50,
                                y_label: "Bps",
                                legend: ['Download', 'Upload'],
                                target: inner,
                                custom_line_color_map: [3, 2]
                            });
                        }
                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("AJAX failed!");
                rainbowBar.hide();
            });
        }

        function buildUsersTimeIndex(users) {
            var self = vm;
            //reset index
            self.usersTimeIndex = {};
            users.forEach(function (user) {
                if (!(user.calling in self.usersTimeIndex)) {
                    self.usersTimeIndex[user.calling] = [];
                    self.CallingPartyIMSIs.push(user.calling + "");
                    if (!(user.productId in self.productIDs)) {
                        self.productIDs.push(user.productId + "")
                    }
                }
                self.usersTimeIndex[user.calling].push(user);
            });
            $scope.callingPartyIMSIs = self.CallingPartyIMSIs;
            $scope.productIDs = self.productIDs;
        }

        function buildCellTowersIndex(cellTowers) {
            //reset index
            vm.cellTowersIndex = {};
            cellTowers.forEach(function (cellTower) {
                vm.cellTowersIndex[cellTower.cellId] = cellTower;
            });
        }

        $scope.subscriberDetails = function (calling) {

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
        }

        $scope.productDetails = function (productId) {

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


    }

})();