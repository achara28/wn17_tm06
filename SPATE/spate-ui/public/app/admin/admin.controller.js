
var am={};
am.basename=$('body').data('key');

function admin() {
    am.iframe.src = am.basename + ":9001";
    console.log('admin');

}

function spark() {
    am.iframe.src=am.basename + ":8080";
    console.log('spark');
}

function yarn() {
    am.iframe.src = am.basename + ":8088";
    console.log('yarn');

}

function hdfs() {
    am.iframe.src = am.basename + ":50070"
    console.log('hdfs');
}

function db() {
    am.iframe.src = am.basename + ":8091";
    console.log('db');
}




(function () {
    'use strict';

    angular
        .module('app')
        .controller('AdminController', AdminController);

    AdminController.$inject = ['AuthenticationService', '$rootScope', '$scope', '$http', 'rainbowBar'];
    function AdminController(AuthenticationService, $rootScope, $scope, $http, rainbowBar) {
        var vm = this;
        vm.selection = "Admin";

        // Base64 encoding service used by AuthenticationService
        var Base64 = AuthenticationService.Base64;
        initController();


        function initController() {
            am.iframe = document.getElementById('iframe');
            am.iframe.src=am.basename + ":9001";

            $(".admin a").on("click", function(){
                $(".admin").find(".active").removeClass("active");
                $(this).parent().addClass("active");
            });

            var height = $(window).height();
            var header = $(".header").height();
            var footer = $(".footer").height();
            $(".container.ng-scope").height(height - header - footer);
            $("#iframe").height(height - header - footer);
            jQuery("body").on("resize", function () {
                var height = $(window).height();
                var header = $(".header").height();
                var footer = $(".footer").height();
                $(".container.ng-scope").height(height-header-footer);
                $("#iframe").height(height - header - footer);

            });

            jQuery("body").on("orientationchange", function () {
                var height = $(window).height();
                var header = $(".header").height();
                var footer = $(".footer").height();
                $(".container.ng-scope").height(height-header-footer);
                $("#iframe").height(height - header - footer);
            });
        }


    }
})();
