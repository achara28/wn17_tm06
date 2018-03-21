(function () {
    'use strict';

    angular
        .module('app', ['ngRoute', 'ngCookies', 'ngRainbow', 'autocomplete'])
        .config(config)
        .run(run);

    config.$inject = ['$routeProvider', '$locationProvider', 'rainbowBarProvider'];
    function config($routeProvider, $locationProvider, rainbowBarProvider) {



        rainbowBarProvider.configure({
            barThickness: 5,
            shadowColor: 'rgba(0, 0, 0, .5)',
            barColors: {
                '0': 'red',
                '.25': 'red',
                '.50': 'red',
                '.75': 'red',
                '1.0': 'red'
            }
        });
        $routeProvider
            .when('/', {
                controller: 'HomeController',
                templateUrl: 'views/home',
                controllerAs: 'vm'
            })
            .when('/login', {
                controller: 'LoginController',
                templateUrl: 'views/login',
                controllerAs: 'vm'
            })
            .when('/:selection', {
                controller: 'HomeController',
                templateUrl: 'views/home',
                controllerAs: 'vm'
            })
            .otherwise({redirectTo: '/login'});
    }

    run.$inject = ['$rootScope', '$location', '$timeout', '$cookieStore', '$http'];
    function run($rootScope, $location,  $timeout, $cookieStore, $http) {

        /**
         * Set al the initial values here
         * @type {string}
         */
        $rootScope.timezone = 'Asia/Nicosia';
        $rootScope.timeout = 30 * 60000;
        $rootScope.live = true;
        $rootScope.showCells = $cookieStore.get('showCells') || !$cookieStore.get('firsttime');
        $rootScope.showHeatmap2g = $cookieStore.get('showHeatmap2g') || !$cookieStore.get('firsttime');
        $rootScope.showHeatmap3g = $cookieStore.get('showHeatmap3g') || !$cookieStore.get('firsttime');
        $rootScope.showHeatmap4g = $cookieStore.get('showHeatmap4g') || !$cookieStore.get('firsttime');
        $rootScope.showCellsCount = $cookieStore.get('showCellsCount') || !$cookieStore.get('firsttime');
        $rootScope.heatMapURL = $('body').data('key') + '/MTN/{z}/{x}/{y}.png';
        $cookieStore.put('firsttime',true);
        $rootScope.newMoment = function (date) {
            if ($rootScope.timezone) return moment(date).tz($rootScope.timezone);
            else return moment(date);
        };

        $rootScope.format = function (date,pattern) {
            var moment = $rootScope.newMoment(date);
            if(pattern)
                return moment.tz($rootScope.timezone).format(pattern);
            return moment.tz($rootScope.timezone).format("YYYY-MM-DD HH:mm");
        };


        // keep user logged in after page refresh
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata; // jshint ignore:line
        }

        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in and trying to access a restricted page
            var restrictedPage = $.inArray($location.path(), ['/login', '/register']) === -1;
            var loggedIn = $rootScope.globals.currentUser;
            if (restrictedPage && !loggedIn) {
                $location.path('/login');
            }
        });

        $rootScope.$watch('showHeatmap2g', function (newVal, oldVal) {
            $cookieStore.put('showHeatmap2g', newVal);
        }, true);
        $rootScope.$watch('showHeatmap3g', function (newVal, oldVal) {
            $cookieStore.put('showHeatmap3g', newVal);
        }, true);
        $rootScope.$watch('showHeatmap4g', function (newVal, oldVal) {
            $cookieStore.put('showHeatmap4g', newVal);
        }, true);
        $rootScope.$watch('showCellsCount', function (newVal, oldVal) {
            $cookieStore.put('showCellsCount', newVal);
        }, true);
        $rootScope.$watch('showCells', function (newVal, oldVal) {
            $cookieStore.put('showCells', newVal);
        }, true);


    }

})();