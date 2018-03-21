(function () {
    'use strict';

    angular
        .module('app')
        .controller('HomeController', HomeController)
        .service('sharedProperties', function ($rootScope) {
            var data = {
                startdate: $rootScope.format($rootScope.newMoment().subtract(30, "minute")),
                enddate: $rootScope.format($rootScope.newMoment())
            };

            return {
                setData: function (value) {
                    data.startdate = value.startdate;
                    data.enddate = value.enddate;
                    $rootScope.$apply();
                }, getData: function () {
                    return data;
                }
            }
        });

    HomeController.$inject = ['$scope', '$rootScope', '$routeParams', 'sharedProperties'];
    function HomeController($scope, $rootScope, $routeParams, sharedProperties) {
        var vm = this;
        vm.timezone = $rootScope.timezone;
        vm.user = null;
        vm.selection = $routeParams.selection;
        vm.appname = "SPATE";
        vm.active = false;
        initController();

        function initController() {
            vm.user = $rootScope.globals.currentUser.username;

            $rootScope.config = {
                flow: 'column'
            };


        }

    }

})();