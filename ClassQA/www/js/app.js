// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'starter.services' is found in services.js
// 'starter.controllers' is found in controllers.js


angular.module('app', ['ionic', 'app.controllers', 'app.routes', 'app.directives','app.services',])

.config(function($ionicConfigProvider, $sceDelegateProvider){

  $sceDelegateProvider.resourceUrlWhitelist([ 'self','*://www.youtube.com/**', '*://player.vimeo.com/video/**']);

})


.value('numOfAnswers',{number:0})
  
.controller('userCtrl', ['$scope', '$http', 'numOfAnswers', function ($scope, $http,numOfAnswers) {
  var url = 'http://localhost:80/getData.php';
  $http.get(url).success(function(response) {
    // Store response data
    //$scope.users = response.data;
    var data = response;
    //numOfAnswers = ({"answer1":data[0].answer1})
    data[0].answer4 = "";
    $scope.data = {Question:data[0].question,answer1:data[0].answer1,answer2:data[0].answer2,answer3:data[0].answer3,answer4:data[0].answer4};
    
    }).error(function(){
      alert("error");
  });
  
 }])

.factory('factoryData',function(){
  var data = {
    id: -1,
    Submitted: false
  };
  return {
    getId: function(){
      return data.id;
    },
    setId: function(id){
      data.id = id;
    },
    setSubmitted: function(cond){
      data.Submitted = cond;
    },
    getSubmitted: function(){
      return data.Submitted;
    }
  };

})



.filter('range', function() {
  return function(input, scope) {
    //alert(parseInt(scope));
    //total = parseInt(scope);
    //alert(total);
    var table = ["A","B","C"];
       
    for (var i in table)
    
      input.push(table[i]);
    return input;
  };
})

.run(function($ionicPlatform,$window,$http,$state) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
      cordova.plugins.Keyboard.disableScroll(true);
    }
    if (window.StatusBar) {
      // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }

    var Obj = JSON.parse($window.localStorage["saved"]);
    if(Obj){
      var url = 'http://localhost:80/Login.php';
      $http.post(url, {'username' : Obj.username,'password':Obj.password}).success(function(response) {
          
          if(response=="Success"){
              $state.go('menu.home');
             
          }
          
          
      })  
    }
    

  });
})

/*
  This directive is used to disable the "drag to open" functionality of the Side-Menu
  when you are dragging a Slider component.
*/
.directive('disableSideMenuDrag', ['$ionicSideMenuDelegate', '$rootScope', function($ionicSideMenuDelegate, $rootScope) {
    return {
        restrict: "A",  
        controller: ['$scope', '$element', '$attrs', function ($scope, $element, $attrs) {

            function stopDrag(){
              $ionicSideMenuDelegate.canDragContent(false);
            }

            function allowDrag(){
              $ionicSideMenuDelegate.canDragContent(true);
            }

            $rootScope.$on('$ionicSlides.slideChangeEnd', allowDrag);
            $element.on('touchstart', stopDrag);
            $element.on('touchend', allowDrag);
            $element.on('mousedown', stopDrag);
            $element.on('mouseup', allowDrag);

        }]
    };
}])

/*
  This directive is used to open regular and dynamic href links inside of inappbrowser.
*/
.directive('hrefInappbrowser', function() {
  return {
    restrict: 'A',
    replace: false,
    transclude: false,
    link: function(scope, element, attrs) {
      var href = attrs['hrefInappbrowser'];

      attrs.$observe('hrefInappbrowser', function(val){
        href = val;
      });
      
      element.bind('click', function (event) {

        window.open(href, '_system', 'location=yes');

        event.preventDefault();
        event.stopPropagation();

      });
    }
  };
});