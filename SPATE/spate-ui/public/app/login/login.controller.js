(function () {
    'use strict';

    angular
        .module('app')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$location', 'AuthenticationService', 'FlashService'];
    function LoginController($location, AuthenticationService, FlashService) {
        var vm = this;
        vm.appname ="SPATE";
        vm.login = login;

        (function initController() {
            // reset login status
            AuthenticationService.ClearCredentials();

            var height = $(window).height();
            var header = $(".header").height();
            var footer = $(".footer").height();
            $(".container.ng-scope").height(height - header - footer);
            $("#sidebar").css({top: header, bottom: footer});
            jQuery("body").on("resize", function () {
                var height = $(window).height();
                var header = $(".header").height();
                var footer = $(".footer").height();
                $(".container.ng-scope").height(height-header-footer);

            });

            jQuery("body").on("orientationchange", function () {
                var height = $(window).height();
                var header = $(".header").height();
                var footer = $(".footer").height();
                $(".container.ng-scope").height(height-header-footer);

            });

        })();




        function login() {
            vm.dataLoading = true;
            AuthenticationService.Login(vm.username, Sha1.hash(vm.password), function (response) {
                if (response.status=="success") {
                    AuthenticationService.SetCredentials(vm.username,Sha1.hash(vm.password));
                    $location.path('/');
                } else {
                    FlashService.Error(response.message);
                    vm.dataLoading = false;
                }
            });
        }


    }

})();
