(function () {
    'use strict';

    angular
        .module('app')
        .controller('LocationController', LocationController);
    LocationController.$inject = ['AuthenticationService', '$cookieStore', '$rootScope', '$location', '$scope', '$http', 'sharedProperties', 'rainbowBar'];
    function LocationController(AuthenticationService, $cookieStore, $rootScope, $location, $scope, $http, sharedProperties, rainbowBar) {
        var vm = this;
        vm.first2gl = true;
        vm.first3gl = true;
        vm.first4gl = true;
        vm.mapCluster = null;
        vm.timeDimensionControl = null;
        vm.authdata = $rootScope.globals.currentUser.authdata;
        vm.selection = $location.path();
        vm.CallingPartyIMSIs = ['All'];
        vm.productIDs = ['All'];
        vm.cellTowersIndex = {};
        vm.usersTimeIndex = {};
        vm.cellTimeIndex = {};
        vm.data = sharedProperties.getData();
        vm.startdate = vm.data.startdate;
        vm.enddate = vm.data.enddate;
        vm.cdrLayers = [];
        vm.group = new L.featureGroup();
        vm.dayGroup = new L.TileLayer.WebGLCoverageMap({
            size: 100,
            opacity: 0.75,
            limit: {min: -100, max: -10},
            legendTitle: "CDR Speed",
            legend: true,
            legenddiv: $("#query-1 #querylegend")
        });
        /* L.TileLayer.Heat({
         size: 40,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -100, max: -10},
         legendTitle: "Download",
         legend: true,
         legenddiv: $("#mtn-map .container .row")
         });*/
        vm.mapCellCluster = new L.MarkerClusterGroup({
            iconCreateFunction: function (cluster) {
                var markers = cluster.getAllChildMarkers();
                var count = markers.length;
                count = count > 1000 ? Math.round(count / 1000) + "k" : count;
                var color = 'blue';
                // Creates an orange marker with the cell tower icon
                var cell = L.AwesomeMarkers.icon({
                    icon: 'wifi',
                    markerColor: color, prefix: 'fa', iconColor: 'white'
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

        vm.open2g = L.tileLayer('https://tiles-prod.opensignal.com/?zoom={z}&x={x}&y={y}&netwkID=31012&&netwkType%5B%5D=3G', {
            maxZoom: 15,
            attribution: "Map data © OpenSignal 2016",
            zIndex: 3,
            opacity: 0.7
        });

        vm.open3g = L.tileLayer('https://tiles-prod.opensignal.com/?zoom={z}&x={x}&y={y}&netwkID=31012&&netwkType%5B%5D=3G', {
            maxZoom: 15,
            attribution: "Map data © OpenSignal 2016",
            zIndex: 3,
            opacity: 0.7
        });

        vm.open4g = L.tileLayer('https://tiles-prod.opensignal.com/?zoom={z}&x={x}&y={y}&netwkID=31012&&netwkType%5B%5D=4G', {
            maxZoom: 15,
            attribution: "Map data © OpenSignal 2016",
            zIndex: 4,
            opacity: 0.7
        });


        /*vm.heatmapLayer2g = new L.TileLayer.WebGLCoverageMap({
         size: 1140,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -100, max: -10},
         legendTitle: "2G coverage",
         legend: true,
         legenddiv: $("#mtn-map .container .row")
         });




         vm.heatmapLayer3g = new L.TileLayer.WebGLCoverageMap({
         size: 1140,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -150, max: -10},
         legendTitle: "3G coverage",
         legend: true,
         legenddiv: $("#mtn-map .container .row")
         });
         vm.heatmapLayer4g = new L.TileLayer.WebGLCoverageMap({
         size: 1140,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -170, max: -10},
         legendTitle: "4G coverage",
         legend: true,
         legenddiv: $("#mtn-map .container .row")
         });
         vm.heatmapLayer2gl = new L.TileLayer.WebGLCoverageMap({
         size: 23,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -100, max: -10},
         legend: false
         });
         vm.heatmapLayer3gl = new L.TileLayer.WebGLCoverageMap({
         size: 23,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -150, max: -10},
         legend: false
         });
         vm.heatmapLayer4gl = new L.TileLayer.WebGLCoverageMap({
         size: 23,
         autoresize: true,
         opacity: 0.5,
         limit: {min: -170, max: -10},
         legend: false
         });
         */
        vm.basename = $('body').data('key');
        vm.calling = "";
        vm.productid = "";
        vm.maxspeed = 100;//100KB
        vm.minspeed = 1;//1KB
        vm.activeQuery = 0;
        // Base64 encoding service used by AuthenticationService
        var Base64 = AuthenticationService.Base64;

        initController();

        intializeSettings();


        loadSideMenu();

        bindEvents();

        function initController() {


            $scope.service = sharedProperties;

            vm.map = L.map('map', {
                center: [32.73, -117.144],
                zoom: 9,
                minZoom: 5,
                zoomControl: false,
            });

            vm.sidebar = L.control.sidebar('sidebar').addTo(vm.map);
            //Add settings button

            vm.map.on('contextmenu', function () {
                vm.map.zoomOut();
            });

            new L.Control.Zoom({position: 'bottomright'}).addTo(vm.map);
            // var osmUrl = 'http://www.openstreetmap.org/openlayers/img/blank.gif',
            //     osmAttribution = 'Map data &copy; 2016 OpenStreetMap contributors',
            //     osm = new L.tileLayer(osmUrl, {maxZoom: 18, attribution: osmAttribution}).addTo(vm.map);
            L.control.locate({position: 'bottomleft'}).addTo(vm.map);

            // Creates a red marker with the user icon
            var user = L.AwesomeMarkers.icon({
                icon: 'user',
                markerColor: 'red', prefix: 'fa', iconColor: 'white'
            });

            var self = this;
            vm.mapCluster = new L.MarkerClusterGroup({
                iconCreateFunction: function (cluster) {
                    if (vm.activeQuery == 0 || vm.activeQuery == 2)
                        return iconNMSCreateFunction(cluster);
                    else if (vm.activeQuery == 4)
                        return iconTHCreateFunction(cluster);
                    else
                        return iconCDRCreateFunction(cluster);
                },
                spiderfyDistanceMultiplier: 3,
                zoomToBoundsOnClick: false
            });

            vm.mapCluster.on('clusterdblclick', function (a) {
                a.layer.zoomToBounds();
            });


            function iconCDRCreateFunction(cluster) {
                var markers = cluster.getAllChildMarkers();
                var count = markers.length;
                var upfluxavg = 0;
                var downfluxavg = 0;
                for (var i = 0; i < count; i++) {
                    upfluxavg += markers[i].upflux * 1.0/* / (markers[i].elapseduration <= 0 ? 0.0001 : markers[i].elapseduration)*/;
                    downfluxavg += markers[i].downflux * 1.0 /*/ (markers[i].elapseduration <= 0 ? 0.0001 : markers[i].elapseduration)*/;
                }
                upfluxavg = upfluxavg * 1.0 / count;
                downfluxavg = downfluxavg * 1.0 / count;

                var speed = upfluxavg;

                if (vm.activeQuery == 1 && $("#query1 .btn-toggle .active input").val() === "download")
                    speed = downfluxavg;

                if (vm.activeQuery == 3 && $("#query3 .btn-toggle .active input").val() === "download")
                    speed = downfluxavg;
                var cell = L.AwesomeMarkers.icon({
                    icon: 'none',
                    markerColor: getColor(speed * 1.0 / (vm.maxspeed * 1000)),
                    prefix: 'fa',
                    iconColor: 'white',
                    html: "<div class='awesome'><span>" + formatBytes(speed * 1.0, 1) + "</span></div>"
                });
                return cell;
            }

            function iconTHCreateFunction(cluster) {
                var markers = cluster.getAllChildMarkers();
                var count = markers.length;
                var speedavg = 0;
                for (var i = 0; i < count; i++) {
                    speedavg += markers[i].speed * 1.0/* / (markers[i].elapseduration <= 0 ? 0.0001 : markers[i].elapseduration)*/;
                }
                speedavg = speedavg / count;


                var speed = speedavg;

                var cell = L.AwesomeMarkers.icon({
                    icon: 'none',
                    markerColor: getColor(speed / (vm.maxspeed)),
                    prefix: 'fa',
                    iconColor: 'white',
                    html: "<div class='awesome'><span>" + speed.toFixed(1) + "</span></div>"
                });
                return cell;
            }

            function formatBytes(bytes, decimals) {
                if (bytes == 0) return '0 Byte';
                var k = 1000; // or 1024 for binary
                var dm = decimals + 1 || 3;
                var sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
                var i = Math.floor(Math.log(bytes) / Math.log(k));
                return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
            }

            function iconNMSCreateFunction(cluster) {
                var markers = cluster.getAllChildMarkers();
                var count = markers.length;
                var drops = 0;
                for (var i = 0; i < count; i++) {
                    drops += markers[i].speed * 1.0;
                }
                drops = drops * 1.0 / count;

                var speed = drops;

                var cell = L.AwesomeMarkers.icon({
                    icon: 'none',
                    markerColor: getColorReverse(speed * 1.0 / vm.maxspeed),
                    prefix: 'fa',
                    iconColor: 'white',
                    html: "<div class='awesome'><span>" + parseFloat(Math.round(speed * 10) / 10).toFixed(1) + "%</span></div>"
                });
                return cell;
            }


            var googleStreets = L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                maxZoom: 18,
                attribution: "Map data ©2015 Google",
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            }).addTo(vm.map);

            var googleTerrain = L.tileLayer('http://{s}.google.com/vt/lyrs=p&x={x}&y={y}&z={z}', {
                maxZoom: 18,
                attribution: "Map data ©2015 Google",
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            });

            var googleHybrid = L.tileLayer('http://{s}.google.com/vt/lyrs=s,h&x={x}&y={y}&z={z}', {
                maxZoom: 18,
                attribution: "Map data ©2015 Google",
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            });

            var googleSat = L.tileLayer('http://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
                maxZoom: 18,
                attribution: "Map data ©2015 Google",
                subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
            });

            vm.layers = L.control.layers({
                "Street": googleStreets,
                "Hybrid": googleHybrid,
                "Terrain": googleTerrain,
                "Satellite": googleSat
            }, null, {position: 'bottomright'});
            vm.layers.addTo(vm.map);


            //addNMSTimePlayer(layers);

            var map = vm.map;
            setTimeout(function () {
                map.invalidateSize();
            }, 10);

            vm.map.addLayer(vm.mapCluster);
            vm.mapCellCluster.on('clusterdblclick', function (a) {
                a.layer.zoomToBounds();
            });
            loadCells($http);

            if ($rootScope.showHeatmap2g) {
                // load2G($http)
                vm.open2g.addTo(vm.map);

            }

            if ($rootScope.showHeatmap3g) {
                // load3G($http)
                vm.open3g.addTo(vm.map);
            }

            if ($rootScope.showHeatmap4g) {
                // load4G($http)
                vm.open4g.addTo(vm.map);
            }

            $scope.$watch('service.getData()', function (newVal, oldVal) {
                if (newVal.startdate === oldVal.startdate && newVal.enddate === oldVal.enddate) {
                    return;
                }
                vm.startdate = newVal.startdate;
                vm.enddate = newVal.enddate;
            }, true);

            $('.sql').click(function () {
                var am = {};
                am.basename = $('body').data('key');
                window.open(am.basename + ":8888/beeswax/");

            });

            //initializeSlider();
            //fixStackProblem(vm.map)
        }

        function initializeSlider() {

            $("#query1 #maxmin").slider({
                range: true,
                min: 1,
                max: 100,
                values: [1, 100],
                slide: function (event, ui) {

                    $("#query1 .active input[name='maxspeed']").val(ui.values[1]);
                    $("#query1 .active input[name='minspeed']").val(ui.values[0]);
                    //$("#amount").val("$" + ui.values[0] + " - $" + ui.values[1]);
                }
            });

            $("#query3 #maxmin").slider({
                range: true,
                min: 1,
                max: 100,
                values: [1, 100],
                slide: function (event, ui) {

                    $("#query3 .active input[name='maxspeed']").val(ui.values[1]);
                    $("#query3 .active input[name='minspeed']").val(ui.values[0]);
                    //$("#amount").val("$" + ui.values[0] + " - $" + ui.values[1]);
                }
            });

            $("#query4 #maxmin").slider({
                range: true,
                min: 1,
                max: 10,
                values: [1, 10],
                slide: function (event, ui) {

                    $("#query4 .active input[name='maxspeed']").val(ui.values[1]);
                    $("#query4 .active input[name='minspeed']").val(ui.values[0]);
                    //$("#amount").val("$" + ui.values[0] + " - $" + ui.values[1]);
                }
            });
        }

        function loadSideMenu() {

            //L.control.fullscreen().addTo(vm.map);
            query0();
            query1();
            query2();
            query3();
            query4();
            initializeSlider();


            var responsePromise = $http.get("/getSize"/*, {headers: {'Authorization': 'Basic ' + vm.authdata}}*/);

            responsePromise.success(function (data, status, headers, config) {

                createGraph(data);
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
            });

        }

        function query0() {

            if ($cookieStore.get('startdate0'))
                vm.startdate = $cookieStore.get('startdate0');
            if ($cookieStore.get('enddate0'))
                vm.enddate = $cookieStore.get('enddate0');
            if ($cookieStore.get('live0') == false) {
                $rootScope.live = $cookieStore.get('live0');
                $('#query0 .live').text("Offline");
                $('#query0 .live').addClass("btn-danger");
                $('#query0 #daterange').prop("disabled", false);
            }


            $('#query0 #daterange').daterangepicker({
                timePicker: true,
                startDate: vm.startdate,
                endDate: vm.enddate,
                singleDatePicker: true,
                timePicker24Hour: true,
                timePickerIncrement: 30,
                locale: {
                    "format": "YYYY-MM-DD @ HH:mm",
                    "separator": " - ",
                    "applyLabel": "Apply",
                    "cancelLabel": "Cancel",
                    "fromLabel": "From",
                    "toLabel": "To",
                    "customRangeLabel": "Custom",
                    "daysOfWeek": [
                        "Su",
                        "Mo",
                        "Tu",
                        "We",
                        "Th",
                        "Fr",
                        "Sa"
                    ],
                    "monthNames": [
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December"
                    ],
                    "firstDay": 1
                },
                "linkedCalendars": false,
                "opens": "left"
            }, function (start, end, label) {
                var data = {
                    startdate: $rootScope.format(start),
                    enddate: $rootScope.format(end.add(30, "minute"))
                };
                sharedProperties.setData(data);
            });

            $('#query0 .daterange i').click(function () {
                $(this).parent().find('input').click();
            });

            var toggle = !$cookieStore.get('live0');

            $('#query0 #daterange').prop("disabled", !toggle);

            $('#query0 .active .live').click(function () {
                var toggle = $('#query0 #daterange').prop("disabled");

                $rootScope.live = $('#query0 .live').text().trim() == "Live";


                if (!$rootScope.live) {
                    var data = {
                        startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                        enddate: $rootScope.format($rootScope.newMoment())
                    };
                    sharedProperties.setData(data);
                    $('#query0 .live').text("Live");
                    $('#query0 .live').removeClass("btn-danger");
                    $('#query0 #daterange').prop("disabled", true);
                } else {
                    $('#query0 .live').text("Offline");
                    $('#query0 .live').addClass("btn-danger");

                    $('#query0 #daterange').prop("disabled", false);
                }
            });

            vm.valuationDatePickerOpen = function () {
                $('#query0 #daterange').data('daterangepicker').toggle();
            };


            $("#query0 #submitQuery").click(function () {

                if (vm.timeDimensionControl != null) {
                    vm.map.removeControl(vm.timeDimensionControl);
                    vm.timeDimensionControl = null;
                }
                $rootScope.live = $('#query0 .live').text().trim() == "Live";


                var date = vm.startdate/*.substring(0, 10)*/;
                vm.maxspeed = 100;
                vm.minspeed = 0;

                vm.activeQuery = 0;

                if ($rootScope.live) {
                    $('#map .live').text("Live");
                    $('#map .live').removeClass("label-danger");
                    $('#map .live').addClass("label-success");
                } else {
                    $('#map .live').text("Offline");
                    $('#map .live').addClass("label-danger");
                }
                $('#activequery').text($('#query0 .sidebar-header').text());
                $('#activequery').click(function () {
                    vm.sidebar.open('query0');
                });
                addNMSMarkerLegend("Call Drops rate", $("#query0 #querylegend"));
                $("#query0 #querylegend").find(".well").remove();
                // if ($("#query0 .btn-toggle .active input").val() === "2g")
                //     $("#query0 #querylegend").append("<div class='well'><p>2G Call Drop Rate = ([CM33:Call Drops on Traffic Channel]*{100})/([K3013A:Successful TCH Seizures (Traffic Channel)]+[CH323:Number of Successful Incoming Internal Inter-Cell Handovers]+[CH343:Successful Incoming External Inter-Cell Handovers]-[CH313:Number of Successful Outgoing Internal Inter-Cell Handovers]-[CH333:Successful Outgoing External Inter-Cell Handovers]+[CH363:Successful Incoming Inter-RAT Inter-Cell Handovers])</p></div>");
                // else
                //     $("#query0 #querylegend").append("<div class='well'><p>3G Call Drop Rate = {100}*[VS.RAB.AbnormRel.CS]/([VS.RAB.NormRel.CS]+[VS.RAB.AbnormRel.CS])</p></div>");

                loadNMSPerDateTime($http, date, vm.startdate, vm.enddate);

                $cookieStore.put('startdate0', vm.startdate);
                $cookieStore.put('enddate0', vm.enddate);
                $cookieStore.put('live0', $rootScope.live);
            });
            $("#query0 #resetQuery").click(function () {
                vm.calling = $("#query1 input[name='calling']").val("");
                vm.productid = $("#query1 input[name='productid']").val("");
                vm.maxspeed = $("#query1 input[name='maxspeed']").val("");
                vm.maxspeed = $("#query1 input[name='minspeed']").val("");
            });
        }

        function query1() {


            if ($cookieStore.get('startdate1'))
                vm.startdate = $cookieStore.get('startdate1');
            if ($cookieStore.get('enddate1'))
                vm.enddate = $cookieStore.get('enddate1');
            if ($cookieStore.get('live1') == false) {
                $rootScope.live = $cookieStore.get('live1');
                $('#query1 .live').text("Offline");
                $('#query1 .live').addClass("btn-danger");
                $('#query1 #daterange').prop("disabled", false);
            }
            if ($cookieStore.get('calling1')) {
                $("#query1 input[name='calling']").val($cookieStore.get('calling1'));

            }
            if ($cookieStore.get('productid1')) {
                $("#query1 input[name='productid']").val($cookieStore.get('productid1'));
            }
            if (vm.timeDimensionControl)
                vm.map.removeControl(vm.timeDimensionControl);

            $('#query1 #daterange').daterangepicker({
                timePicker: true,
                startDate: vm.startdate,
                endDate: vm.enddate,
                singleDatePicker: true,
                timePicker24Hour: true,
                timePickerIncrement: 30,
                locale: {
                    "format": "YYYY-MM-DD @ HH:mm",
                    "separator": " - ",
                    "applyLabel": "Apply",
                    "cancelLabel": "Cancel",
                    "fromLabel": "From",
                    "toLabel": "To",
                    "customRangeLabel": "Custom",
                    "daysOfWeek": [
                        "Su",
                        "Mo",
                        "Tu",
                        "We",
                        "Th",
                        "Fr",
                        "Sa"
                    ],
                    "monthNames": [
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December"
                    ],
                    "firstDay": 1
                },
                "linkedCalendars": false,
                "opens": "left"
            }, function (start, end, label) {
                var data = {
                    startdate: $rootScope.format(start),
                    enddate: $rootScope.format(end.add(30, "minute"))
                };
                sharedProperties.setData(data);
            });

            $('#query1 .daterange i').click(function () {
                $(this).parent().find('input').click();
            });

            var toggle = !$cookieStore.get('live1');

            $('#query1 #daterange').prop("disabled", !toggle);

            $('#query1 .active .live').click(function () {
                var toggle = $('#query1 #daterange').prop("disabled");
                $rootScope.live = $('#query1 .live').text().trim() == "Live";
                if (!$rootScope.live) {
                    var data = {
                        startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                        enddate: $rootScope.format($rootScope.newMoment())
                    };
                    sharedProperties.setData(data);
                    $('#query1 .live').text("Live");
                    $('#query1 .live').removeClass("btn-danger");
                    $('#daterange').prop("disabled", true);
                } else {
                    $('#query1 .live').text("Offline");
                    $('#query1 .live').addClass("btn-danger");

                    $('#query1 #daterange').prop("disabled", false);
                }

            });

            vm.valuationDatePickerOpen = function () {
                $('#query1 #daterange').data('daterangepicker').toggle();
            };


            $("#query1 #submitQuery").click(function () {

                if (vm.timeDimensionControl != null) {
                    vm.map.removeControl(vm.timeDimensionControl);
                    vm.timeDimensionControl = null;
                }
                $rootScope.live = $('#query1 .live').text().trim() == "Live";

                vm.activeQuery = 1;
                var date = vm.startdate/*.substring(0, 10)*/;
                vm.calling = $("#query1 input[name='calling']").val();
                vm.productid = $("#query1 input[name='productid']").val();
                var maxspeed = $("#query1 input[name='maxspeed']").val();
                var minspeed = $("#query1 input[name='minspeed']").val();


                if (maxspeed.trim().length > 0) {
                    vm.maxspeed = parseFloat(maxspeed);
                }
                if (minspeed.trim().length > 0) {
                    vm.minspeed = parseFloat(minspeed);
                }

                if ($rootScope.live) {
                    $('#map .live').text("Live");
                    $('#map .live').removeClass("label-danger");
                    $('#map .live').addClass("label-success");
                } else {
                    $('#map .live').text("Offline");
                    $('#map .live').addClass("label-danger");
                }
                $('#activequery').text($('#query1 .sidebar-header').text());
                $('#activequery').click(function () {
                    vm.sidebar.open('query1');
                });
                addMarkerLegend("CDR DownFlux/UpFlux (KB)", $("#query1 #querylegend"));
                loadCDRPerDateTime($http, date, vm.startdate, vm.enddate);
                $cookieStore.put('calling1', vm.calling);
                $cookieStore.put('productid1', vm.productid);
                $cookieStore.put('startdate1', vm.startdate);
                $cookieStore.put('enddate1', vm.enddate);
                $cookieStore.put('live1', $rootScope.live);
            });
            $("#query1 #resetQuery").click(function () {
                vm.calling = $("#query1 input[name='calling']").val("");
                vm.productid = $("#query1 input[name='productid']").val("");
                vm.maxspeed = $("#query1 input[name='maxspeed']").val("");
                vm.maxspeed = $("#query1 input[name='minspeed']").val("");
            });
            $("#query1 input[name='maxspeed']").numeric(".");
            $("#query1 input[name='minspeed']").numeric(".");
            $("#query1 input[name='maxspeed']").val(vm.maxspeed);
            $("#query1 input[name='minspeed']").val(vm.minspeed);
        }

        function query2() {

            if ($cookieStore.get('startdate2'))
                vm.startdate = $cookieStore.get('startdate2');
            if ($cookieStore.get('enddate2'))
                vm.enddate = $cookieStore.get('enddate2');
            if ($cookieStore.get('live2') == false) {
                $rootScope.live = $cookieStore.get('live2');
                $('#query2 .live').text("Offline");
                $('#query2 .live').addClass("btn-danger");
                $('#query2 #daterange').prop("disabled", false);
            }

            $('#query2 #daterange').daterangepicker({
                timePicker: true,
                startDate: vm.startdate,
                endDate: vm.enddate,
                timePicker24Hour: true,
                timePickerIncrement: 30,
                locale: {
                    "format": "YYYY-MM-DD @ HH:mm",
                    "separator": " - ",
                    "applyLabel": "Apply",
                    "cancelLabel": "Cancel",
                    "fromLabel": "From",
                    "toLabel": "To",
                    "customRangeLabel": "Custom",
                    "daysOfWeek": [
                        "Su",
                        "Mo",
                        "Tu",
                        "We",
                        "Th",
                        "Fr",
                        "Sa"
                    ],
                    "monthNames": [
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December"
                    ],
                    "firstDay": 1
                },
                "linkedCalendars": false,
                "opens": "left"
            }, function (start, end, label) {
                var data = {
                    startdate: $rootScope.format(start),
                    enddate: $rootScope.format(end.add(30, "minute"))
                };
                sharedProperties.setData(data);
            });

            $('#query2 .daterange i').click(function () {
                $(this).parent().find('input').click();
            });

            var toggle = !$cookieStore.get('live2');
            ;

            $('#query2 #daterange').prop("disabled", !toggle);

            $('#query2 .active .live').click(function () {
                var toggle = $('#query2 #daterange').prop("disabled");
                $rootScope.live = $('#query2 .live').text().trim() == "Live";
                if (!$rootScope.live) {
                    var data = {
                        startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                        enddate: $rootScope.format($rootScope.newMoment())
                    };
                    sharedProperties.setData(data);
                    $('#query2 .live').text("Live");
                    $('#query2 .live').removeClass("btn-danger");
                    $('#query2 #daterange').prop("disabled", true);
                } else {
                    $('#query2 .live').text("Offline");
                    $('#query2 .live').addClass("btn-danger");

                    $('#query2 #daterange').prop("disabled", false);
                }
            });

            vm.valuationDatePickerOpen = function () {
                $('#query2 #daterange').data('daterangepicker').toggle();
            };


            $("#query2 #submitQuery").click(function () {

                if (vm.timeDimensionControl != null) {
                    vm.map.removeControl(vm.timeDimensionControl);
                    vm.timeDimensionControl = null;
                }
                $rootScope.live = $('#query2 .live').text().trim() == "Live";
                var date = vm.startdate/*.substring(0, 10)*/;
                vm.maxspeed = 100;
                vm.minspeed = 0;

                vm.activeQuery = 2;

                if ($rootScope.live) {
                    $('#map .live').text("Live");
                    $('#map .live').removeClass("label-danger");
                    $('#map .live').addClass("label-success");
                } else {
                    $('#map .live').text("Offline");
                    $('#map .live').addClass("label-danger");
                }
                $('#activequery').text($('#query2 .sidebar-header').text());
                $('#activequery').click(function () {
                    vm.sidebar.open('query2');
                });
                addNMSMarkerLegend("Call Drops rate", $("#query2 #querylegend"));
                var url = "/tnms3g";

                $("#query2 #querylegend").find(".well").remove();
                // if ($("#query2 .btn-toggle .active input").val() === "2g") {
                //     url = "/tnms2g";
                //     $("#query2 #querylegend").append("<div class='well'><p>2G Call Drop Rate = ([CM33:Call Drops on Traffic Channel]*{100})/([K3013A:Successful TCH Seizures (Traffic Channel)]+[CH323:Number of Successful Incoming Internal Inter-Cell Handovers]+[CH343:Successful Incoming External Inter-Cell Handovers]-[CH313:Number of Successful Outgoing Internal Inter-Cell Handovers]-[CH333:Successful Outgoing External Inter-Cell Handovers]+[CH363:Successful Incoming Inter-RAT Inter-Cell Handovers])</p></div>");
                // } else
                //     $("#query2 #querylegend").append("<div class='well'><p>3G Call Drop Rate = {100}*[VS.RAB.AbnormRel.CS]/([VS.RAB.NormRel.CS]+[VS.RAB.AbnormRel.CS])</p></div>");

                addNMSTimePlayer(vm.layers, date, vm.startdate, vm.enddate, url);

                $cookieStore.put('startdate2', vm.startdate);
                $cookieStore.put('enddate2', vm.enddate);
                $cookieStore.put('live2', $rootScope.live);
            });
            $("#query2 #resetQuery").click(function () {
                vm.calling = $("#query2 input[name='calling']").val("");
                vm.productid = $("#query2 input[name='productid']").val("");
                vm.maxspeed = $("#query2 input[name='maxspeed']").val("");
                vm.maxspeed = $("#query2 input[name='minspeed']").val("");
            });
        }

        function query3() {

            if ($cookieStore.get('startdate3'))
                vm.startdate = $cookieStore.get('startdate3');
            if ($cookieStore.get('enddate3'))
                vm.enddate = $cookieStore.get('enddate3');
            if ($cookieStore.get('live3') == false) {
                $rootScope.live = $cookieStore.get('live3');
                $('#query3 .live').text("Offline");
                $('#query3 .live').addClass("btn-danger");

                $('#query3 #daterange').prop("disabled", false);

            }
            if ($cookieStore.get('calling3')) {
                $("#query3 input[name='calling']").val($cookieStore.get('calling3'));

            }
            if ($cookieStore.get('productid3')) {
                $("#query3 input[name='productid']").val($cookieStore.get('productid3'));
            }

            $('#query3 #daterange').daterangepicker({
                timePicker: true,
                startDate: vm.startdate,
                endDate: vm.enddate,
                timePicker24Hour: true,
                timePickerIncrement: 30,
                locale: {
                    "format": "YYYY-MM-DD @ HH:mm",
                    "separator": " - ",
                    "applyLabel": "Apply",
                    "cancelLabel": "Cancel",
                    "fromLabel": "From",
                    "toLabel": "To",
                    "customRangeLabel": "Custom",
                    "daysOfWeek": [
                        "Su",
                        "Mo",
                        "Tu",
                        "We",
                        "Th",
                        "Fr",
                        "Sa"
                    ],
                    "monthNames": [
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December"
                    ],
                    "firstDay": 1
                },
                "linkedCalendars": false,
                "opens": "left"
            }, function (start, end, label) {

                var data = {
                    startdate: $rootScope.format(start),
                    enddate: $rootScope.format(end.add(30, "minute"))
                };
                sharedProperties.setData(data);
            });

            $('#query3 .daterange i').click(function () {
                $(this).parent().find('input').click();
            });

            var toggle = !$cookieStore.get('live3');

            $('#query3 #daterange').prop("disabled", !toggle);

            $('#query3 .active .live').click(function () {
                var toggle = $('#query3 #daterange').prop("disabled");
                $rootScope.live = $('#query3 .live').text().trim() == "Live";

                if (!$rootScope.live) {
                    var data = {
                        startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                        enddate: $rootScope.format($rootScope.newMoment())
                    };
                    sharedProperties.setData(data);
                    $('#query3 .live').text("Live");
                    $('#query3 .live').removeClass("btn-danger");
                    $('#daterange').prop("disabled", true);
                } else {
                    $('#query3 .live').text("Offline");
                    $('#query3 .live').addClass("btn-danger");

                    $('#query3 #daterange').prop("disabled", false);
                }

            });

            vm.valuationDatePickerOpen = function () {
                $('#query3 #daterange').data('daterangepicker').toggle();
            };


            $("#query3 #submitQuery").click(function () {
                if (vm.timeDimensionControl != null) {
                    vm.map.removeControl(vm.timeDimensionControl);
                    vm.timeDimensionControl = null;
                }
                $rootScope.live = $('#query3 .live').text().trim() == "Live";
                vm.activeQuery = 3;

                var date = vm.startdate/*.substring(0, 10)*/;


                vm.calling = $("#query3 input[name='calling']").val();
                vm.productid = $("#query3 input[name='productid']").val();
                var maxspeed = $("#query3 input[name='maxspeed']").val();
                var minspeed = $("#query3 input[name='minspeed']").val();


                if (maxspeed.trim().length > 0) {
                    vm.maxspeed = parseFloat(maxspeed);
                }
                if (minspeed.trim().length > 0) {
                    vm.minspeed = parseFloat(minspeed);
                }

                if ($rootScope.live) {
                    $('#map .live').text("Live");
                    $('#map .live').removeClass("label-danger");
                    $('#map .live').addClass("label-success");
                } else {
                    $('#map .live').text("Offline");
                    $('#map .live').addClass("label-danger");
                }

                $('#activequery').text($('#query3 .sidebar-header').text());
                $('#activequery').click(function () {
                    vm.sidebar.open('query3');
                });
                addMarkerLegend("CDR DownFlux/UpFlux (KB)", $("#query3 #querylegend"));
                var url = "/tcdr";
                addCDRTimePlayer(vm.layers, date, vm.startdate, vm.enddate, url);
                $cookieStore.put('calling3', vm.calling);
                $cookieStore.put('productid3', vm.productid);
                $cookieStore.put('startdate3', vm.startdate);
                $cookieStore.put('enddate3', vm.enddate);
                $cookieStore.put('live3', $rootScope.live);
            });
            $("#query3 #resetQuery").click(function () {
                vm.calling = $("#query1 input[name='calling']").val("");
                vm.productid = $("#query1 input[name='productid']").val("");
                vm.maxspeed = $("#query1 input[name='maxspeed']").val("");
                vm.maxspeed = $("#query1 input[name='minspeed']").val("");
            });
            $("#query3 input[name='maxspeed']").numeric(".");
            $("#query3 input[name='minspeed']").numeric(".");
            $("#query3 input[name='maxspeed']").val(vm.maxspeed);
            $("#query3 input[name='minspeed']").val(vm.minspeed);
        }

        function query4() {

            if ($cookieStore.get('startdate4'))
                vm.startdate = $cookieStore.get('startdate4');
            if ($cookieStore.get('enddate4'))
                vm.enddate = $cookieStore.get('enddate4');
            if ($cookieStore.get('live4') == false) {
                $rootScope.live = $cookieStore.get('live4');
                $('#query4 .live').text("Offline");
                $('#query4 .live').addClass("btn-danger");
                $('#query4 #daterange').prop("disabled", false);
            }


            $('#query4 #daterange').daterangepicker({
                timePicker: true,
                startDate: vm.startdate,
                endDate: vm.enddate,
                singleDatePicker: true,
                timePicker24Hour: true,
                timePickerIncrement: 30,
                locale: {
                    "format": "YYYY-MM-DD @ HH:mm",
                    "separator": " - ",
                    "applyLabel": "Apply",
                    "cancelLabel": "Cancel",
                    "fromLabel": "From",
                    "toLabel": "To",
                    "customRangeLabel": "Custom",
                    "daysOfWeek": [
                        "Su",
                        "Mo",
                        "Tu",
                        "We",
                        "Th",
                        "Fr",
                        "Sa"
                    ],
                    "monthNames": [
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December"
                    ],
                    "firstDay": 1
                },
                "linkedCalendars": false,
                "opens": "left"
            }, function (start, end, label) {
                var data = {
                    startdate: $rootScope.format(start),
                    enddate: $rootScope.format(end.add(30, "minute"))
                };
                sharedProperties.setData(data);
            });

            $('#query4 .daterange i').click(function () {
                $(this).parent().find('input').click();
            });

            var toggle = !$cookieStore.get('live4');

            $('#query4 #daterange').prop("disabled", !toggle);

            $('#query4 .active .live').click(function () {
                var toggle = $('#query4 #daterange').prop("disabled");

                $rootScope.live = $('#query4 .live').text().trim() == "Live";


                if (!$rootScope.live) {
                    var data = {
                        startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                        enddate: $rootScope.format($rootScope.newMoment())
                    };
                    sharedProperties.setData(data);
                    $('#query4 .live').text("Live");
                    $('#query4 .live').removeClass("btn-danger");
                    $('#query4 #daterange').prop("disabled", true);
                } else {
                    $('#query4 .live').text("Offline");
                    $('#query4 .live').addClass("btn-danger");

                    $('#query4 #daterange').prop("disabled", false);
                }
            });

            vm.valuationDatePickerOpen = function () {
                $('#query4 #daterange').data('daterangepicker').toggle();
            };


            $("#query4 #submitQuery").click(function () {

                if (vm.timeDimensionControl != null) {
                    vm.map.removeControl(vm.timeDimensionControl);
                    vm.timeDimensionControl = null;
                }
                vm.maxspeed = 10;
                vm.minspeed = 0;

                var maxspeed = $("#query4 input[name='maxspeed']").val();
                var minspeed = $("#query4 input[name='minspeed']").val();


                if (maxspeed.trim().length > 0) {
                    vm.maxspeed = parseFloat(maxspeed);
                }
                if (minspeed.trim().length > 0) {
                    vm.minspeed = parseFloat(minspeed);
                }


                $rootScope.live = $('#query4 .live').text().trim() == "Live";


                var date = vm.startdate/*.substring(0, 10)*/;

                vm.activeQuery = 4;

                if ($rootScope.live) {
                    $('#map .live').text("Live");
                    $('#map .live').removeClass("label-danger");
                    $('#map .live').addClass("label-success");
                } else {
                    $('#map .live').text("Offline");
                    $('#map .live').addClass("label-danger");
                }
                $('#activequery').text($('#query4 .sidebar-header').text());
                $('#activequery').click(function () {
                    vm.sidebar.open('query4');
                });

                /*
                 * Add the Legends and the equation
                 */
                addTHMarkerLegend("3G User Throughput", $("#query4 #querylegend"));
                $("#query4 #querylegend").find(".well").remove();
                // $("#query4 #querylegend").append("<div class='well'><p>3G User Throughput = VS.HSDPA.MeanChThroughput.TotalBytes * 8 / 1024 / 1024 / 1800</p></div>");

                loadTHNMSPerDateTime($http, date, vm.startdate, vm.enddate);

                $cookieStore.put('startdate4', vm.startdate);
                $cookieStore.put('enddate4', vm.enddate);
                $cookieStore.put('live4', $rootScope.live);
            });
            $("#query4 #resetQuery").click(function () {
                vm.maxspeed = $("#query4 input[name='maxspeed']").val("");
                vm.maxspeed = $("#query4 input[name='minspeed']").val("");
            });
        }

        function addNMSTimePlayer(layers, date, start, end, url) {

            var mdate = (date + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var startDate = (start + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var endDate = (end + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");

            vm.dataToRequest = {"date": mdate, "start": startDate, "end": endDate}

            if (vm.calling.trim().length > 0 && vm.productid.trim().length > 0) {
                url = "/tspcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim(),
                    "productid": vm.productid.trim()
                }
            }
            else if (vm.calling.trim().length > 0) {
                url = "/tscdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim()
                }
            }
            else if (vm.productid.trim().length > 0) {
                url = "/tpcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "productid": vm.productid.trim()
                }
            }

            if (vm.timeDimensionControl != null)
                vm.map.removeControl(vm.timeDimensionControl);

            // start of TimeDimension manual instantiation
            var timeDimension = new L.TimeDimension({

                timeInterval: start + "/" + end,
                period: "PT1800S",
                // times: [startDate, startDate]
            });
// helper to share the timeDimension object between all layers
            vm.map.timeDimension = timeDimension;
// otherwise you have to set the 'timeDimension' option on all layers.

            var player = new L.TimeDimension.Player({
                transitionTime: 2000,
                loop: false,
                buffer: 1,
                minBufferReady: -1,
            }, timeDimension);

            var timeDimensionControlOptions = {
                player: player,
                timeDimension: timeDimension,
                position: 'bottomleft',
                autoPlay: true,
                minSpeed: 1,
                speedStep: 0.5,
                speedSlider: false,
                maxSpeed: 15
            };

            vm.timeDimensionControl = new L.Control.TimeDimension(timeDimensionControlOptions);
            vm.map.addControl(vm.timeDimensionControl);


            //var geojsonURL = vm.basename + ":9000/cdrperday";

            var geoTimeLayer = L.timeDimension.layer.ajaxGeoJSON(vm.map, {
                updateTimeDimension: true,
                addlastPoint: true,
                waitForReady: true,
                url: url,
                data: vm.dataToRequest,
                authdata: vm.authdata
            });

            geoTimeLayer.on('timeload', function (obj) {
                var self = vm;
                var data = obj.data;
                self.mapCluster.removeLayers(self.cdrLayers);
                // and bulk load the cell towers
                self.cdrLayers = [];

                buildCellsTimeIndex(data);


                var index = vm.cellTimeIndex;


                Object.keys(index).forEach(function (key) {
                    var speed = 0;
                    var point = null;
                    if ($("#query0 .btn-toggle .active input").val() === "3g") {
                        if (index[key].length < 2)
                            return;
                        var abnormRel = index[key][0];
                        var normRel = index[key][1];
                        //if the counters are wrong then swao
                        if (index[key][0].counter == 67179779) {
                            var temp = abnormRel;
                            abnormRel = normRel;
                            normRel = temp;
                        }

                        if (abnormRel.value > 0)
                            speed = 100 * abnormRel.value / (normRel.value + abnormRel.value);
                        point = abnormRel;
                    } else {

                        var CM33 = 0;
                        var K3013A = 0;
                        var CH323 = 0;
                        var CH343 = 0;
                        var CH313 = 0;
                        var CH333 = 0;
                        var CH363 = 0;

                        for (var i = 0; i < index[key].length; i++) {

                            /*([1278072498->CM33:Call Drops on Traffic Channel]*{100})/
                             ([1278087432->K3013A:Successful TCH Seizures (Traffic Channel)]+
                             [1278078459->CH323:Number of Successful Incoming Internal Inter-Cell Handovers]+
                             [1278080467->CH343:Successful Incoming External Inter-Cell Handovers]-
                             [1278079528->CH313:Number of Successful Outgoing Internal Inter-Cell Handovers]-
                             [1278081557->CH333:Successful Outgoing External Inter-Cell Handovers]+
                             [1278082436->CH363:Successful Incoming Inter-RAT Inter-Cell Handovers])*/
                            switch (index[key][i].counter) {
                                case "1278072498":
                                    CM33 = index[key][i].value;
                                    break;
                                case "1278087432":
                                    K3013A = index[key][i].value;
                                    break;
                                case "1278078459":
                                    CH323 = index[key][i].value;
                                    break;
                                case "1278080467":
                                    CH343 = index[key][i].value;
                                    break;
                                case "1278079528":
                                    CH313 = index[key][i].value;
                                    break;
                                case "1278081557":
                                    CH333 = index[key][i].value;
                                    break;
                                case "1278082436":
                                    CH363 = index[key][i].value;
                                    break;
                            }
                        }
                        if (CM33 > 0)
                            speed = (CM33 * 100.0) / (K3013A + CH323 + CH343 - CH313 - CH333 + CH363);
                        point = index[key][0];
                    }


                    var user = L.AwesomeMarkers.icon({
                        icon: 'none',
                        markerColor: getColorReverse(speed * 1.0 / vm.maxspeed),
                        prefix: 'fa',
                        iconColor: 'white',
                        html: "<div class='awesome'><span>" + speed.toFixed(1) + "%</span></div>"
                    });

                    var marker = L.marker([point[0], point[1]], {icon: user});

                    marker.type = "drops";
                    marker.cellid = point[2];
                    marker.counter = point[3];
                    marker.speed = speed;
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    //{"mytimestamp":"20160111013000","latitude":34.6837696,"cellid":"280100021115081","value":0,"longitude":32.6989312}
                    domelem.innerHTML = '<table id="' + "table_" + key + '"><thead>' +
                        '<tr><th data-field="2">cellid</th>' +
                        '<th data-field="3">value (' + speed + ')</th>' +
                        '<th data-field="5">mytimestamp</th> <th data-field="4">counter</th>' +
                        '</tr></thead></table>';

                    marker.on('popupopen', function () {
                        //reset inner html
                        var data = index[key];
                        $("#table_" + key).bootstrapTable({
                            data: data
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
            });

            geoTimeLayer.addTo(vm.map);

        }


        function addCDRTimePlayer(layers, date, start, end, url) {
            var mdate = (date + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var startDate = (start + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var endDate = (end + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            vm.dataToRequest = {"date": mdate, "start": startDate, "end": endDate}

            if (vm.calling.trim().length > 0 && vm.productid.trim().length > 0) {
                url = "/tspcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim(),
                    "productid": vm.productid.trim()
                }
            }
            else if (vm.calling.trim().length > 0) {
                url = "/tscdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim()
                }
            }
            else if (vm.productid.trim().length > 0) {
                url = "/tpcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "productid": vm.productid.trim()
                }
            }


            if (vm.timeDimensionControl != null)
                vm.map.removeControl(vm.timeDimensionControl);

            // start of TimeDimension manual instantiation
            var timeDimension = new L.TimeDimension({

                timeInterval: start + "/" + end,
                period: "PT1800S",
                // times: [startDate, startDate]
            });
// helper to share the timeDimension object between all layers
            vm.map.timeDimension = timeDimension;
// otherwise you have to set the 'timeDimension' option on all layers.

            var player = new L.TimeDimension.Player({
                transitionTime: 2000,
                loop: false,
                buffer: 1,
                minBufferReady: -1,
            }, timeDimension);

            var timeDimensionControlOptions = {
                player: player,
                timeDimension: timeDimension,
                position: 'bottomleft',
                autoPlay: true,
                minSpeed: 1,
                speedStep: 0.5,
                speedSlider: false,
                maxSpeed: 15
            };

            vm.timeDimensionControl = new L.Control.TimeDimension(timeDimensionControlOptions);
            vm.map.addControl(vm.timeDimensionControl);


            //var geojsonURL = vm.basename + ":9000/cdrperday";

            var geoTimeLayer = L.timeDimension.layer.ajaxGeoJSON(vm.map, {
                updateTimeDimension: true,
                addlastPoint: true,
                waitForReady: true,
                url: url,
                data: vm.dataToRequest,
                authdata: vm.authdata
            });

            geoTimeLayer.on('timeload', function (obj) {
                var self = vm;
                var data = obj.data;
                self.mapCluster.removeLayers(self.cdrLayers);
                // and bulk load the cell towers
                self.cdrLayers = [];
                vm.CallingPartyIMSIs = [];
                vm.productIDs = [];
                vm.usersTimeIndex = {};

                // Creates a blue marker with the user icon

                buildUsersTimeIndex(data);


                var index = vm.usersTimeIndex;

                Object.keys(index).forEach(function (key) {
                    /*
                     0:35.151088
                     1:33.35936
                     2:"20151125093529"
                     3:"35796673199"
                     4:"303002"
                     5:3106988
                     6:373658
                     7:225
                     */
                    var detail = index[key][index[key].length - 1];


                    var speed = detail[6] * 1.0/* / (detail.elapseduration <= 0 ? 0.0001 : detail.elapseduration)*/;
                    if ($("#query3 .btn-toggle .active input").val() === "download")
                        speed = detail[5] * 1.0 /*/ (detail.elapseduration <= 0 ? 0.0001 : detail.elapseduration)*/;

                    var user = L.AwesomeMarkers.icon({
                        icon: 'user',
                        markerColor: getColor(speed * 1.0 / (vm.maxspeed * 1000)), prefix: 'fa', iconColor: 'white'
                    });

                    var marker = L.marker([detail[0], detail[1]], {icon: user});

                    marker.type = "user";
                    marker.calling = detail[3];
                    marker.downflux = detail[7];
                    marker.upflux = detail[6];
                    marker.elapseduration = detail.elapseduration;
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    domelem.innerHTML = '<table id="' + "table_" + key + '"><thead>' +
                        // '<tr><th data-field="3">callingpartynumber</th>' +
                        '<th data-field="6">upflux</th>' +
                        '<th data-field="5">downflux</th> <th data-field="7">elapseduration</th>' +
                        '<th data-field="2">mytimestamp</th>' +
                        // '<th data-field="4">productid</th>' +
                        '</tr></thead></table>';

                    marker.on('popupopen', function () {
                        //reset inner html
                        var data = index[key];
                        $("#table_" + key).bootstrapTable({
                            data: data
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
            });

            geoTimeLayer.addTo(vm.map);

        }

        function bindEvents() {


            var height = $(window).height();
            var header = 0;//$(".header").height();
            var footer = 0;//$(".footer").height();
            $("#map").height(height - header - footer);
            $("#sidebar").css({top: header, bottom: footer});
            jQuery(window).on("resize", function () {
                var height = $(window).height();
                var header = 0;//$(".header").height();
                var footer = 0;//$(".footer").height();
                $("#map").height(height - header - footer);
                $("#sidebar").css({top: header, bottom: footer});
                window.setTimeout(function () {
                    invalidateUI();
                }, 600);

            });

            jQuery(window).on("orientationchange", function () {
                var height = $(window).height();
                var header = 0;//$(".header").height();
                var footer = 0;//$(".footer").height();
                $("#map").height(height - header - footer);
                $("#sidebar").css({top: header, bottom: footer});
                window.setTimeout(function () {
                    invalidateUI();
                }, 600);

            });

            $('.collapse').on('shown.bs.collapse', function (e) {
                var height = $(window).height();
                var header = 0;//$(".header").height();
                var footer = 0;//$(".footer").height();
                $("#map").height(height - header - footer);
                $("#sidebar").css({top: header, bottom: footer});
                window.setTimeout(function () {
                    invalidateUI();
                }, 600);
            });

            $('.collapse').on('hidden.bs.collapse', function (e) {
                var height = $(window).height();
                var header = 0;//$(".header").height();
                var footer = 0;//$(".footer").height();
                $("#map").height(height - header - footer);
                $("#sidebar").css({top: header, bottom: footer});
                window.setTimeout(function () {
                    invalidateUI();
                }, 600);
            });


            $('.btn-toggle').click(function () {
                $(this).find('.btn').toggleClass('active');

                if ($(this).find('.btn-primary').size() > 0) {
                    $(this).find('.btn').toggleClass('btn-primary');
                }
                if ($(this).find('.btn-danger').size() > 0) {
                    $(this).find('.btn').toggleClass('btn-danger');
                }
                if ($(this).find('.btn-success').size() > 0) {
                    $(this).find('.btn').toggleClass('btn-success');
                }
                if ($(this).find('.btn-info').size() > 0) {
                    $(this).find('.btn').toggleClass('btn-info');
                }

                $(this).find('.btn').toggleClass('btn-default');

            });

            $('#GroupedSwitches').on('switchChange.bootstrapSwitch', function () {
                $('#GroupedSwitches').bootstrapSwitch('state');
            });

            //Switches

            $('input[name="show-cells"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    loadCells($http);
                }
                else {
                    if (vm.mapCellCluster)
                        vm.mapCellCluster.clearLayers();
                }
                $rootScope.showCells = state;
                //sync the switches
                $('input[name="show-cells"]').bootstrapSwitch('state', state, true);
            });

            $('input[name="show-heatmap2g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {

                    vm.open2g.addTo(vm.map);
                    //     if (vm.heatmapLayer2g.data.length <= 0)
                    //         load2G($http);
                    //     else {
                    //         if (vm.map.getZoom() < 18)
                    //             vm.map.addLayer(vm.heatmapLayer2g);
                    //         else
                    //             vm.map.addLayer(vm.heatmapLayer2gl);
                    //     }
                }
                else {
                    vm.map.removeLayer(vm.open2g);
                    //     if (vm.heatmapLayer2g)
                    //         vm.map.removeLayer(vm.heatmapLayer2g);
                    //     if (vm.heatmapLayer2gl)
                    //         vm.map.removeLayer(vm.heatmapLayer2gl);
                }
                $rootScope.showHeatmap2g = state;
                //Load the corresponding cell towers
                //$rootScope.showCells = false;
                $('input[name="show-cells"]').bootstrapSwitch('state', $rootScope.showCells, true);
                loadCells($http);

                //sync the switches
                $('input[name="show-heatmap2g"]').bootstrapSwitch('state', state, true);
            });

            $('input[name="show-heatmap3g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    vm.open3g.addTo(vm.map);
                    //     if (vm.heatmapLayer3g.data.length <= 0)
                    //         load3G($http);
                    //     else {
                    //         if (vm.map.getZoom() < 18)
                    //             vm.map.addLayer(vm.heatmapLayer3g);
                    //         else
                    //             vm.map.addLayer(vm.heatmapLayer3gl);
                    //     }
                }
                else {
                    vm.map.removeLayer(vm.open3g);
                    //     if (vm.heatmapLayer3g)
                    //         vm.map.removeLayer(vm.heatmapLayer3g);
                    //     if (vm.heatmapLayer3gl)
                    //         vm.map.removeLayer(vm.heatmapLayer3gl);
                }
                $rootScope.showHeatmap3g = state;
                //Load the corresponding cell towers
                //$rootScope.showCells = false;
                $('input[name="show-cells"]').bootstrapSwitch('state', $rootScope.showCells, true);
                loadCells($http);
                //sync the switches
                $('input[name="show-heatmap3g"]').bootstrapSwitch('state', state, true);
            });

            $('input[name="show-heatmap4g"]').on('switchChange.bootstrapSwitch', function (event, state) {
                if (state) {
                    vm.open4g.addTo(vm.map);
                    //     if (vm.heatmapLayer4g.data.length <= 0)
                    //         load4G($http);
                    //     else {
                    //         if (vm.map.getZoom() < 18)
                    //             vm.map.addLayer(vm.heatmapLayer4g);
                    //         else
                    //             vm.map.addLayer(vm.heatmapLayer4gl);
                    //     }
                }
                else {
                    vm.map.removeLayer(vm.open4g);
                    //     if (vm.heatmapLayer4g)
                    //         vm.map.removeLayer(vm.heatmapLayer4g);
                    //     if (vm.heatmapLayer4gl)
                    //         vm.map.removeLayer(vm.heatmapLayer4gl);
                }
                $rootScope.showHeatmap4g = state;
                //Load the corresponding cell towers
                //Load the corresponding cell towers
                //$rootScope.showCells = false;
                $('input[name="show-cells"]').bootstrapSwitch('state', $rootScope.showCells, true);
                loadCells($http);
                //sync the switches
                $('input[name="show-heatmap4g"]').bootstrapSwitch('state', state, true);
            });

            $('input[name="show-cells-count"]').on('switchChange.bootstrapSwitch', function (event, state) {

                $rootScope.showCellsCount = state;
                if (vm.mapCellCluster)
                    vm.mapCellCluster.clearLayers();
                loadCells($http);

                //sync the switches
                $('input[name="show-cells-count"]').bootstrapSwitch('state', state, true);
            });

            // $('[data-toggle="tooltip"]').tooltip({'placement': 'bottom'});
        }

        function invalidateUI() {
            vm.map.invalidateSize();

            /*
             if ($rootScope.showHeatmap2g)
             vm.heatmapLayer2g.resize();
             if ($rootScope.showHeatmap3g)
             vm.heatmapLayer3g.resize();
             if ($rootScope.showHeatmap4g)
             vm.heatmapLayer4g.resize();
             */

        }

        function shadeColor(color, percent) {
            var num = parseInt(color.slice(1), 16), amt = Math.round(2.55 * percent), R = (num >> 16) + amt, G = (num >> 8 & 0x00FF) + amt, B = (num & 0x0000FF) + amt;
            return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 + (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1);
        }


        var geocoder = new google.maps.Geocoder();

        function googleGeocoding(text, callResponse) {
            geocoder.geocode({address: text}, callResponse);
        }

        function formatJSON(rawjson) {
            var json = {},
                key, loc, disp = [];

            for (var i in rawjson) {
                key = rawjson[i].formatted_address;

                loc = L.latLng(rawjson[i].geometry.location.lat(), rawjson[i].geometry.location.lng());

                json[key] = loc;	//key,value format
            }

            return json;
        }

        function intializeSettings() {

            /*
             * Initialize search
             */

            var input = document.getElementById("searchBox");
            //32.7343822 and the longitude is: -117.14412270000003.
            var defaultBounds = new google.maps.LatLngBounds(
                new google.maps.LatLng(32.0000, -117.0000),
                new google.maps.LatLng(32.0000, -118.0000));
            var options = {
                bounds: defaultBounds,
                types: ['(cities)'],
                componentRestrictions: {country: "us"}
            };

            var searchBox = new google.maps.places.SearchBox(input, options);


            searchBox.addListener('places_changed', function () {
                var places = searchBox.getPlaces();

                if (places.length == 0) {
                    return;
                }

                vm.group.clearLayers();

                places.forEach(function (place) {

                    // Create a marker for each place.
                    var marker = L.marker([
                        place.geometry.location.lat(),
                        place.geometry.location.lng()
                    ]);
                    vm.group.addLayer(marker);
                });

                vm.group.addTo(vm.map);
                vm.map.fitBounds(vm.group.getBounds());

            });

            /*
             * Initialize checkboxes
             */

            vm.map.addControl(new checkbox({
                position: 'topright',
                name: 'show-heatmap2g',
                title: "Show Predicted Coverage Map (2G)",
                text: "2G"
            }));


            vm.map.addControl(new checkbox({
                position: 'topright',
                name: 'show-heatmap3g',
                title: "Show Predicted Coverage Map (3G)",
                text: "3G"
            }));
            vm.map.addControl(new checkbox({
                position: 'topright',
                name: 'show-heatmap4g',
                title: "Show Predicted Coverage Map (4G)",
                text: "4G"
            }));

            vm.map.addControl(new livebutton({
                position: 'topleft',
                title: "When “Live” is green, presented results are based on measurements collected during the last 30 minutes.",
                text: ""
            }));


            $('input[name="show-cells"]').bootstrapSwitch({
                size: "mini",
                state: $rootScope.showCells,
                onColor: "warning"
            }, true);
            $('input[name="show-cells-count"]').bootstrapSwitch({
                size: "mini",
                state: $rootScope.showCellsCount,
                onColor: "warning"
            }, true);
            $('input[name="show-heatmap2g"]').bootstrapSwitch({
                size: "mini",
                state: $rootScope.showHeatmap2g
            }, true);
            $('input[name="show-heatmap3g"]').bootstrapSwitch({
                size: "mini",
                state: $rootScope.showHeatmap3g
            }, true);
            $('input[name="show-heatmap4g"]').bootstrapSwitch({
                size: "mini",
                state: $rootScope.showHeatmap4g
            }, true);


            //$('[tdata-toggle="tooltip"]').tooltip();
        }

        function load2G($http) {
            rainbowBar.show();
            //var geojsonURL = 'http://tile.example.com/{z}/{x}/{y}.json';
            var geojsonURL = vm.basename + ':9000/2gl/{x}/{y}/{z}';
            var geojsonTileLayer = new L.TileLayer.GeoJSON(geojsonURL, {}, {});
            geojsonTileLayer._tileLoaded = function (tile, tilePoint) {
                if ($rootScope.showHeatmap2g)
                    if (vm.map.getZoom() > 17) {
                        vm.map.removeLayer(vm.heatmapLayer2g);
                        if (tile.datum) {
                            vm.heatmapLayer2gl.addDataWithLimit(tile.datum);
                            vm.map.addLayer(vm.heatmapLayer2gl);
                            if (vm.first2gl) {
                                vm.first2gl = false;
                                window.setTimeout(function () {
                                    vm.heatmapLayer2gl.update();
                                    vm.heatmapLayer2gl.display();
                                }, 600);

                            }
                        }
                    } else {
                        vm.first2gl = true;
                        if ($rootScope.showHeatmap2g && !vm.map.hasLayer(vm.heatmapLayer2g))
                            vm.map.addLayer(vm.heatmapLayer2g);
                        if (vm.map.hasLayer(vm.heatmapLayer2gl)) {
                            vm.map.removeLayer(vm.heatmapLayer2gl);
                            //vm.heatmapLayer2gl.clearLayers();
                        }
                    }

            };
            vm.map.addLayer(geojsonTileLayer);

            var responsePromise = $http.get("/2G", {
                // headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                if (!$rootScope.showHeatmap2g) {
                    rainbowBar.hide();
                    return;
                }
                var a = 0.005;
                vm.map.removeLayer(vm.heatmapLayer2g);
                vm.heatmapLayer2g.addDataWithLimit(data);
                vm.map.addLayer(vm.heatmapLayer2g);
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                rainbowBar.hide();
            });
        }

        function load3G($http) {
            rainbowBar.show();
            //var geojsonURL = 'http://tile.example.com/{z}/{x}/{y}.json';
            var geojsonURL = vm.basename + ':9000/3gl/{x}/{y}/{z}';
            var geojsonTileLayer = new L.TileLayer.GeoJSON(geojsonURL, {}, {});
            geojsonTileLayer._tileLoaded = function (tile, tilePoint) {
                if ($rootScope.showHeatmap3g)
                    if (vm.map.getZoom() > 17) {
                        vm.map.removeLayer(vm.heatmapLayer3g);
                        if (tile.datum) {
                            vm.heatmapLayer3gl.addDataWithLimit(tile.datum);
                            vm.map.addLayer(vm.heatmapLayer3gl);
                            if (vm.first3gl) {
                                vm.first3gl = false;
                                window.setTimeout(function () {
                                    vm.heatmapLayer3gl.update();
                                    vm.heatmapLayer3gl.display();
                                }, 600);

                            }
                        }
                    } else {
                        vm.first3gl = true;
                        if ($rootScope.showHeatmap3g && !vm.map.hasLayer(vm.heatmapLayer3g))
                            vm.map.addLayer(vm.heatmapLayer3g);
                        if (vm.map.hasLayer(vm.heatmapLayer3gl)) {
                            vm.map.removeLayer(vm.heatmapLayer3gl);
                            //vm.heatmapLayer3gl.clearLayers();
                        }
                    }

            };
            vm.map.addLayer(geojsonTileLayer);

            var responsePromise = $http.get("/3G", {
                // headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                var a = 0.005;
                if (!$rootScope.showHeatmap3g) {
                    rainbowBar.hide();
                    return;
                }
                vm.map.removeLayer(vm.heatmapLayer3g);
                vm.heatmapLayer3g.addDataWithLimit(data);
                vm.map.addLayer(vm.heatmapLayer3g);
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                rainbowBar.hide();
            });

        }

        function load4G($http) {
            rainbowBar.show();
            //var geojsonURL = 'http://tile.example.com/{z}/{x}/{y}.json';
            var geojsonURL = vm.basename + ':9000/4gl/{x}/{y}/{z}';
            var geojsonTileLayer = new L.TileLayer.GeoJSON(geojsonURL, {}, {});
            geojsonTileLayer._tileLoaded = function (tile, tilePoint) {
                if ($rootScope.showHeatmap4g)
                    if (vm.map.getZoom() > 17) {

                        vm.map.removeLayer(vm.heatmapLayer4g);
                        if (tile.datum) {
                            vm.heatmapLayer4gl.addDataWithLimit(tile.datum);
                            vm.map.addLayer(vm.heatmapLayer4gl);
                            if (vm.first4gl) {
                                vm.first4gl = false;
                                window.setTimeout(function () {
                                    vm.heatmapLayer4gl.update();
                                    vm.heatmapLayer4gl.display();
                                }, 600);

                            }
                        }


                    } else {
                        vm.first4gl = true;
                        if ($rootScope.showHeatmap4g && !vm.map.hasLayer(vm.heatmapLayer4g))
                            vm.map.addLayer(vm.heatmapLayer4g);
                        if (vm.map.hasLayer(vm.heatmapLayer4gl)) {
                            vm.map.removeLayer(vm.heatmapLayer4gl);
                            //vm.heatmapLayer4gl.clearLayers();
                        }
                    }
                rainbowBar.hide();
            };

            vm.map.addLayer(geojsonTileLayer);

            var responsePromise = $http.get("/4G", {
                // headers: {'Authorization': 'Basic ' + vm.authdata},
                timeout: 300000
            });

            responsePromise.success(function (data, status, headers, config) {
                if (!$rootScope.showHeatmap4g) {
                    rainbowBar.hide();
                    return;
                }
                vm.map.removeLayer(vm.heatmapLayer4g);
                vm.heatmapLayer4g.addDataWithLimit(data);
                vm.map.addLayer(vm.heatmapLayer4g);
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                rainbowBar.hide();
            });
        }

        function loadCells($http) {
            var responsePromise = $http.get("/cellTowers"/*, {headers: {'Authorization': 'Basic ' + vm.authdata}}*/);

            responsePromise.success(function (data, status, headers, config) {
                //if ($rootScope.showCells) {
                loadGUI(data);
                //}
                buildCellTowersIndex(data);
                //Set refresh rate for the map
                //setInterval(loadCDR($http, vm.startdate, vm.enddate), $rootScope.timeout)
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
            });
        }

        function loadGUI(data) {


            vm.mapCellCluster.clearLayers();


            // Creates an blue marker with the cell tower icon
            var cell = L.AwesomeMarkers.icon({
                icon: 'wifi',
                markerColor: 'blue', prefix: 'fa', iconColor: 'white'
            });
            // and bulk load the cell towers
            var layers = [];
            var offset = 0.00023;
            data.forEach(function (cellTower) {
                /*
                 0:"ANP_002"
                 1:"134144513"
                 2:"E_ANP_002_ANONYMOUS_1"
                 3:5240021
                 4:341
                 5:1
                 6:34.9884992
                 7:33.979664
                 8:"280105240021"
                 9:"4G"
                 */

                var marker = L.marker([cellTower[6], cellTower[7] - offset], {icon: cell});
                marker.type = "cellTower";
                marker.cellTower = cellTower;
                marker.cellType = cellTower[9];

                if (!$rootScope.showCells)
                    return;
                if (!$rootScope.showHeatmap2g && marker.cellType == "2G")
                    return;
                if (!$rootScope.showHeatmap3g && marker.cellType == "3G")
                    return;
                if (!$rootScope.showHeatmap4g && marker.cellType == "4G")
                    return;

                layers.push(marker);
                var title = "<p>" +
                    "<span>Cell Id: " + cellTower[8] + "</span><br/>" +
                    "<span>Cell Name: " + cellTower[0] + "</span>" +
                    "</p>";
                var description = "<p>" +
                    "<span>Supports " + cellTower[9] + "</span><br/>" +
                    "<span>" + cellTower[2] + "</span>" +
                    "</p>";

                marker.bindPopup(title + description);
            });
            vm.mapCellCluster.addLayers(layers);

            vm.map.addLayer(vm.mapCellCluster);
        }

        function loadCDRPerDateTime($http, date, start, end) {
            var start_time = new Date().getTime();
            if (vm.selection !== $location.path()) {
                return;
            }
            var self = this;
            rainbowBar.show();
            var mdate = (date + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var startDate = (start + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var endDate = (end + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var url = "/tcdr";

            vm.dataToRequest = {"date": mdate, "start": startDate, "end": endDate}

            if (vm.calling.trim().length > 0 && vm.productid.trim().length > 0) {
                url = "/tspcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim(),
                    "productid": vm.productid.trim()
                }
            }
            else if (vm.calling.trim().length > 0) {
                url = "/tscdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "callingpartynumber": vm.calling.trim()
                }
            }
            else if (vm.productid.trim().length > 0) {
                url = "/tpcdr";
                vm.dataToRequest = {
                    "date": mdate,
                    "start": startDate,
                    "end": endDate,
                    "productid": vm.productid.trim()
                }
            }

            var responsePromise = $http.post(url, vm.dataToRequest, {
                timeout: 30000,
                // 'Authorization': 'Basic ' + vm.authdata,
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

                // Creates a blue marker with the user icon

                buildUsersTimeIndex(data);


                var index = vm.usersTimeIndex;

                Object.keys(index).forEach(function (key) {
                    /*
                     0:35.151088
                     1:33.35936
                     2:"20151125093529"
                     3:"35796673199"
                     4:"303002"
                     5:3106988
                     6:373658
                     7:225
                     */
                    var detail = index[key][index[key].length - 1];


                    var speed = detail[6] * 1.0/* / (detail.elapseduration <= 0 ? 0.0001 : detail.elapseduration)*/;
                    if ($("#query1 .btn-toggle .active input").val() === "download")
                        speed = detail[5] * 1.0 /*/ (detail.elapseduration <= 0 ? 0.0001 : detail.elapseduration)*/;

                    var user = L.AwesomeMarkers.icon({
                        icon: 'user',
                        markerColor: getColor(speed * 1.0 / (vm.maxspeed * 1000)), prefix: 'fa', iconColor: 'white'
                    });

                    var marker = L.marker([detail[0], detail[1]], {icon: user});

                    marker.type = "user";
                    marker.calling = detail[3];
                    marker.downflux = detail[7];
                    marker.upflux = detail[6];
                    marker.elapseduration = detail[7];
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    domelem.innerHTML = '<table id="' + "table_" + key + '"><thead>' +
                        // '<tr><th data-field="3">callingpartynumber</th>' +
                        '<th data-field="6">upflux</th>' +
                        '<th data-field="5">downflux</th> <th data-field="7">elapseduration</th>' +
                        '<th data-field="2">mytimestamp</th> ' +
                        // '<th data-field="4">productid</th>' +
                        '</tr></thead></table>';

                    marker.on('popupopen', function () {
                        //reset inner html
                        var data = index[key];
                        $("#table_" + key).bootstrapTable({
                            data: data
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
        }

        function loadTHNMSPerDateTime($http, date, start, end) {
            var start_time = new Date().getTime();
            if (vm.selection !== $location.path()) {
                return;
            }
            var self = this;
            rainbowBar.show();
            var mdate = (date + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var startDate = (start + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var endDate = (end + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var url = "/thnms3g";

            vm.dataToRequest = {"date": mdate, "start": startDate, "end": endDate}

            var responsePromise = $http.post(url, vm.dataToRequest, {
                timeout: 30000,
                // 'Authorization': 'Basic ' + vm.authdata,
                params: vm.dataToRequest
            });

            responsePromise.success(function (data, status, headers, config) {

                var self = vm;
                self.mapCluster.removeLayers(self.cdrLayers);
                // and bulk load the cell towers
                self.cdrLayers = [];

                buildCellsTimeIndex(data);


                var index = vm.cellTimeIndex;


                Object.keys(index).forEach(function (key) {

                    var point = index[key][0];
                    //VS.HSDPA.MeanChThroughput.TotalBytes * 8 / 1024 / 1024 / 1800
                    var speed = (((point[4] * 8) / 1024) / 1024) / 1800;


                    var user = L.AwesomeMarkers.icon({
                        icon: 'none',
                        markerColor: getColor(speed / vm.maxspeed),
                        prefix: 'fa',
                        iconColor: 'white',
                        html: "<div class='awesome'><span>" + speed.toFixed(1) + "</span></div>"
                    });

                    var marker = L.marker([point[0], point[1]], {icon: user});

                    marker.type = "user";
                    marker.cellid = point[2];
                    marker.counter = point[3];
                    marker.speed = speed;
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    //{"mytimestamp":"20160111013000","latitude":34.6837696,"cellid":"280100021115081","value":0,"longitude":32.6989312}

                    // [35.0724992,33.3232992,"31342","67189840",9912482,"201607130600"]
                    domelem.innerHTML = '<table id="' + "table_" + key + '"><thead>' +
                        '<tr><th data-field="2">cellid</th>' +
                        '<th data-field="4">value (' + speed.toFixed(1) + ')</th>' +
                        '<th data-field="5">mytimestamp</th> <th data-field="3">counter</th>' +
                        '</tr></thead></table>';

                    marker.on('popupopen', function () {
                        //reset inner html
                        var data = index[key];
                        $("#table_" + key).bootstrapTable({
                            data: data
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
        }

        function loadNMSPerDateTime($http, date, start, end) {
            var start_time = new Date().getTime();
            if (vm.selection !== $location.path()) {
                return;
            }
            var self = this;
            rainbowBar.show();
            var mdate = (date + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var startDate = (start + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var endDate = (end + "").replace(/-/g, "").replace(/ /g, "").replace(/:/g, "");
            var url = "/tnms3g";

            if ($("#query0 .btn-toggle .active input").val() === "2g")
                url = "/tnms2g";

            vm.dataToRequest = {"date": mdate, "start": startDate, "end": endDate}

            var responsePromise = $http.post(url, vm.dataToRequest, {
                timeout: 30000,
                // 'Authorization': 'Basic ' + vm.authdata,
                params: vm.dataToRequest
            });

            responsePromise.success(function (data, status, headers, config) {

                var self = vm;
                self.mapCluster.removeLayers(self.cdrLayers);
                // and bulk load the cell towers
                self.cdrLayers = [];

                buildCellsTimeIndex(data);


                var index = vm.cellTimeIndex;


                Object.keys(index).forEach(function (key) {
                    var speed = 0;
                    var point = null;
                    if ($("#query0 .btn-toggle .active input").val() === "3g") {
                        if (index[key].length < 2)
                            return;
                        var abnormRel = index[key][0];
                        var normRel = index[key][1];
                        //if the counters are wrong then swao
                        if (index[key][0].counter == 67179779) {
                            var temp = abnormRel;
                            abnormRel = normRel;
                            normRel = temp;
                        }

                        if (abnormRel.value > 0)
                            speed = 100 * abnormRel.value / (normRel.value + abnormRel.value);
                        point = abnormRel;
                    } else {

                        var CM33 = 0;
                        var K3013A = 0;
                        var CH323 = 0;
                        var CH343 = 0;
                        var CH313 = 0;
                        var CH333 = 0;
                        var CH363 = 0;

                        for (var i = 0; i < index[key].length; i++) {

                            /*([1278072498->CM33:Call Drops on Traffic Channel]*{100})/
                             ([1278087432->K3013A:Successful TCH Seizures (Traffic Channel)]+
                             [1278078459->CH323:Number of Successful Incoming Internal Inter-Cell Handovers]+
                             [1278080467->CH343:Successful Incoming External Inter-Cell Handovers]-
                             [1278079528->CH313:Number of Successful Outgoing Internal Inter-Cell Handovers]-
                             [1278081557->CH333:Successful Outgoing External Inter-Cell Handovers]+
                             [1278082436->CH363:Successful Incoming Inter-RAT Inter-Cell Handovers])*/
                            switch (index[key][i].counter) {
                                case "1278072498":
                                    CM33 = index[key][i].value;
                                    break;
                                case "1278087432":
                                    K3013A = index[key][i].value;
                                    break;
                                case "1278078459":
                                    CH323 = index[key][i].value;
                                    break;
                                case "1278080467":
                                    CH343 = index[key][i].value;
                                    break;
                                case "1278079528":
                                    CH313 = index[key][i].value;
                                    break;
                                case "1278081557":
                                    CH333 = index[key][i].value;
                                    break;
                                case "1278082436":
                                    CH363 = index[key][i].value;
                                    break;
                            }
                        }
                        if (CM33 > 0)
                            speed = (CM33 * 100.0) / (K3013A + CH323 + CH343 - CH313 - CH333 + CH363);
                        point = index[key][0];
                    }


                    var user = L.AwesomeMarkers.icon({
                        icon: 'none',
                        markerColor: getColorReverse(speed * 1.0 / vm.maxspeed),
                        prefix: 'fa',
                        iconColor: 'white',
                        html: "<div class='awesome'><span>" + speed.toFixed(1) + "%</span></div>"
                    });

                    var marker = L.marker([point[0], point[1]], {icon: user});

                    marker.type = "drops";
                    marker.cellid = point[2];
                    marker.counter = point[3];
                    marker.speed = point[4];
                    var domelem = document.createElement('div');
                    domelem.id = "graph_" + key;
                    //{"mytimestamp":"20160111013000","latitude":34.6837696,"cellid":"280100021115081","value":0,"longitude":32.6989312}
                    domelem.innerHTML = '<table id="' + "table_" + key + '"><thead>' +
                        '<tr><th data-field="2">cellid</th>' +
                        '<th data-field="3">value (' + speed + ')</th>' +
                        '<th data-field="5">mytimestamp</th> <th data-field="4">counter</th>' +
                        '</tr></thead></table>';

                    marker.on('popupopen', function () {
                        //reset inner html
                        var data = index[key];
                        $("#table_" + key).bootstrapTable({
                            data: data
                        });


                    });

                    marker.bindPopup(domelem);

                    self.cdrLayers.push(marker);
                });


                vm.mapCluster.addLayers(self.cdrLayers);
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
            responsePromise.error(function (data, status, headers, config) {
                bootstrap_alert.warning("AJAX failed!");
                var request_time = new Date().getTime() - start_time;
                $('#time').text("The query took: "+request_time+"ms");
                rainbowBar.hide();
            });
        }

        function getColor(d) {
            var max = vm.maxspeed;
            var min = vm.minspeed;
            var minmax = (max - min) * 1.0 / 5;
            var grades = [max * 1.0 / max, (min + 4 * minmax) * 1.0 / max, (min + 3 * minmax) * 1.0 / max, (min + 2 * minmax) * 1.0 / max, (min + minmax) * 1.0 / max];
            return d > grades[1] ? 'green' :
                d > grades[2] ? 'darkgreen' :
                    d > grades[3] ? 'beige' :
                        d > grades[4] ? 'orange' :
                            'red';

        }

        function getColorReverse(d) {
            var max = vm.maxspeed;
            var min = vm.minspeed;
            var minmax = (max - min) * 1.0 / 5;
            var grades = [max * 1.0 / max, (min + 4 * minmax) * 1.0 / max, (min + 3 * minmax) * 1.0 / max, (min + 2 * minmax) * 1.0 / max, (min + minmax) * 1.0 / max];
            return d <= grades[4] ? 'green' :
                d <= grades[3] ? 'darkgreen' :
                    d <= grades[2] ? 'beige' :
                        d <= grades[1] ? 'orange' :
                            'red';

        }

        function hashCode(str) {
            var hash = 0;
            if (str.length == 0) return hash;
            for (var i = 0; i < str.length; i++) {
                var char = str.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash; // Convert to 32bit integer
            }
            return hash;
        }

        function addMarkerLegend(legendTitle, legenddiv) {
            var max = vm.maxspeed;
            var min = vm.minspeed;
            var minmax = (max - min) * 1.0 / 5;
            var grades = [max, (min + 4 * minmax), (min + 3 * minmax), (min + 2 * minmax), (min + minmax)];
            var innerHTML = '<div class="center-block"><h4>' + legendTitle + '</h4><br/></div>';
            var cls = hashCode(legendTitle);
            var divider = max;
            for (var i = 0; i < grades.length; i++) {
                var color = getColor((grades[i] * 1.0 / (divider)));
                var user = L.AwesomeMarkers.icon({
                    icon: 'user',
                    markerColor: color, prefix: 'fa', iconColor: 'white'
                });
                var element = user.createIcon();

                innerHTML +=
                    '<div style="position: absolute; margin-top: 25px;">' + element.outerHTML + '</div> <div class="legend-label">'
                    + (grades.length > i + 1 ? Math.round(grades[i]) : '<' + Math.round(grades[i])) +
                    (grades.length > i + 1 ? ' &ndash; ' + Math.round(grades[i + 1]) + '</div><br>' : '');
            }
            legenddiv.find(".chart-legend_" + cls).remove();
            legenddiv.append('<div class="panel panel-default chart-legend_' + cls + '"> <div class="panel-body">' + innerHTML + "</div></div>");
        }


        function addTHMarkerLegend(legendTitle, legenddiv) {
            var max = vm.maxspeed;
            var min = vm.minspeed;
            var minmax = (max - min) * 1.0 / 5;
            var grades = [max, (min + 4 * minmax), (min + 3 * minmax), (min + 2 * minmax), (min + minmax)];
            var innerHTML = '<div class="center-block"><h4>' + legendTitle + '</h4><br/></div>';
            var cls = hashCode(legendTitle);
            var divider = max;
            for (var i = 0; i < grades.length; i++) {
                var color = getColor((grades[i] * 1.0 / (divider)));
                var user = L.AwesomeMarkers.icon({
                    icon: 'wifi',
                    markerColor: color, prefix: 'fa', iconColor: 'white'
                });
                var element = user.createIcon();

                innerHTML +=
                    '<div style="position: absolute; margin-top: 25px;">' + element.outerHTML + '</div> <div class="legend-label">'
                    + (grades.length > i + 1 ? Math.round(grades[i]) : '<' + Math.round(grades[i])) +
                    (grades.length > i + 1 ? ' &ndash; ' + Math.round(grades[i + 1]) + '</div><br>' : '');
            }
            legenddiv.find(".chart-legend_" + cls).remove();
            legenddiv.append('<div class="panel panel-default chart-legend_' + cls + '"> <div class="panel-body">' + innerHTML + "</div></div>");
        }

        function addNMSMarkerLegend(legendTitle, legenddiv) {
            var max = vm.maxspeed;
            var min = vm.minspeed;
            var minmax = (max - min) * 1.0 / 5;
            var grades = [max, (min + 4 * minmax), (min + 3 * minmax), (min + 2 * minmax), (min + minmax)];
            var innerHTML = '<div class="center-block"><h4>' + legendTitle + '</h4><br/></div>';
            var cls = hashCode(legendTitle);
            for (var i = 0; i < grades.length; i++) {
                var color = getColorReverse((grades[i] * 1.0 / (max)));
                var user = L.AwesomeMarkers.icon({
                    icon: 'user',
                    markerColor: color, prefix: 'fa', iconColor: 'white'
                });
                var element = user.createIcon();

                innerHTML +=
                    '<div style="position: absolute; margin-top: 25px;">' + element.outerHTML + '</div> <div class="legend-label">'
                    + (grades.length > i + 1 ? Math.round(grades[i]) : '<' + Math.round(grades[i])) +
                    (grades.length > i + 1 ? '% &ndash; ' + Math.round(grades[i + 1]) + '%</div><br>' : '%');
            }
            legenddiv.find(".chart-legend_" + cls).remove();
            legenddiv.append('<div class="panel panel-default chart-legend_' + cls + '"> <div class="panel-body">' + innerHTML + "</div></div>");
        }

        function buildUsersTimeIndex(users) {
            var self = vm;
            //reset index
            /*
             0:35.151088
             1:33.35936
             2:"20151125093529"
             3:"35796673199"
             4:"303002"
             5:3106988
             6:373658
             7:225
             */
            self.usersTimeIndex = {};
            users.forEach(function (user) {
                var callingpartynumber = user[3];
                var productid = user[4];
                if (!(callingpartynumber in self.usersTimeIndex)) {
                    self.usersTimeIndex[callingpartynumber] = [];
                    self.CallingPartyIMSIs.push(callingpartynumber);
                    if (!(productid in self.productIDs)) {
                        self.productIDs.push(productid)
                    }

                    self.usersTimeIndex[callingpartynumber].push(user);
                }
            });
            $scope.callingPartyIMSIs = self.CallingPartyIMSIs;
            $scope.productIDs = self.productIDs;
        }

        function buildCellsTimeIndex(cells) {
            var self = vm;
            //reset index
            self.cellTimeIndex = {};
            cells.forEach(function (cell) {
                var cellid = cell[2];
                if (!(cellid in self.cellTimeIndex)) {
                    self.cellTimeIndex[cellid] = [];
                }
                self.cellTimeIndex[cellid].push(cell);
            });

        }

        function buildCellTowersIndex(cellTowers) {
            //reset index
            vm.cellTowersIndex = {};
            cellTowers.forEach(function (cellTower) {
                vm.cellTowersIndex[cellTower[8]] = cellTower;
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
        };


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


        function createGraph(data) {

            var barData = [{
                'x': 'RAW',
                'y': data[1]/1024/1024/1024
            }, {
                'x': 'SPATE',
                'y': data[0]/1024/1024/1024
            }];

            var vis = d3.select('.chart'),
                WIDTH = 400,
                HEIGHT = 300,
                MARGINS = {
                    top: 20,
                    right: 20,
                    bottom: 20,
                    left: 50
                },
                xRange = d3.scale.ordinal().rangeRoundBands([MARGINS.left, WIDTH - MARGINS.right], 0.1).domain(barData.map(function (d) {
                    return d.x;
                })),


                yRange = d3.scale.linear().range([HEIGHT - MARGINS.top, MARGINS.bottom]).domain([0,
                    d3.max(barData, function (d) {
                        return d.y;
                    })
                ]),

                xAxis = d3.svg.axis()
                    .scale(xRange)
                    .tickSize(5)
                    .tickSubdivide(true),

                yAxis = d3.svg.axis()
                    .scale(yRange)
                    .tickSize(5)
                    .orient("left")
                    .tickSubdivide(true);


            vis.append('svg:g')
                .attr('class', 'x axis')
                .attr('transform', 'translate(0,' + (HEIGHT - MARGINS.bottom) + ')')
                .call(xAxis);

            vis.append('svg:g')
                .attr('class', 'y axis')
                .attr('transform', 'translate(' + (MARGINS.left) + ',0)')
                .call(yAxis);

            vis.append("text")      // text label for the x axis
                .attr("transform", "rotate(-90)")
                .attr("x", -180)
                .attr("y", 0)
                .attr("dy", "1em")
                .style("text-anchor", "middle")
                .text("Size (GB)");

            vis.selectAll('rect')
                .data(barData)
                .enter()
                .append('rect')
                .attr('x', function (d) {
                    return xRange(d.x);
                })
                .attr('y', function (d) {
                    return yRange(d.y);
                })
                .attr('width', xRange.rangeBand())
                // add this attribute to change the color of the text
                .attr("fill", function (d) {
                    if (d.x === "RAW") {
                        return "darkred";
                    }
                    return "darkgreen";
                })
                .attr('height', function (d) {
                    return ((HEIGHT - MARGINS.bottom) - yRange(d.y));
                });

        }

    }

})();