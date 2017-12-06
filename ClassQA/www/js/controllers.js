angular.module('app.controllers', [])
  
.controller('homeCtrl', ['$scope', '$stateParams', 'factoryData','$window', // The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams,factoryData,$window) {
    
    
    
}])

.controller('LoginCtrl', ['$scope', '$stateParams', 'factoryData', '$http' ,'$state','$window', // The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams,factoryData,$http,$state,$window) {
    
   
    $scope.data = {};
   
   $scope.submit = function(){
        if(!$scope.data.username){
            alert("username cannot be empty");
            return;
        }
        if(!$scope.data.password){
            alert("Password cannot be empty");
            return;
        }
        var url = 'http://localhost:80/Login.php';
        $http.post(url, {'username' : $scope.data.username,'password':$scope.data.password}).success(function(response) {
            
            if(response=="Success"){
                $state.go('menu.home');

                var myObj = {
                    username: $scope.data.username,
                    password: $scope.data.password,
                
                }
                $window.localStorage["saved"] = JSON.stringify(myObj);
                
                
            }
            else{
                alert(response);
            }
            
        }).error(function(response){
            alert("Wrong username or password");
        })   
   }

   

}])
   
.controller('answerQuestionsCtrl', ['$scope', '$stateParams','$http','factoryData','$window', '$timeout', '$state', // The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams,$http,factoryData,$window,$timeout,$state) {
    
    var timer;
    $scope.counter = 15;
    var updateCounter = function() {
        $scope.counter--;
        if($scope.counter<=0){
            factoryData.setSubmitted(false);
            $scope.cancelTimer();
            $timer = null;
        }
        timer = $timeout(updateCounter, 1000);
    };
    updateCounter();

    $scope.cancelTimer = function(){
        $timeout.cancel(timer);
        $state.go("menu.unansweredQuestions");

    }

    $scope.$on("$destroy", function (event)  
    {  
        $timeout.cancel(timer);  
    }); 
    
    $scope.submitted=factoryData.getSubmitted();
    
    $scope.submit = function(){
        var url = 'http://localhost:80/SubmitAnswer.php';
        $http.post(url, {'id' : $scope.no,'answer':$scope.data.mySelect}).success(function(response) {
            alert("Answer Submitted");
            $scope.submitted = true;
            factoryData.setSubmitted(true);
        }).error(function(response){
            alert(response);
        })    
    }
    $scope.no = factoryData.getId();
    var url = 'http://localhost:80/getUnQuestion.php';
    $http.post(url, {'id' : $scope.no}).success(function(response) {
      // Store response data
      //$scope.users = response.data;
      var data = response;
      $scope.data = {Question:data[0].question,answer1:data[0].answer1,answer2:data[0].answer2,answer3:data[0].answer3,answer4:data[0].answer4};
      var answers = [];
        if(data[0].answer1){
          answers.push({answer:'a'});
        }
        if(data[0].answer2){
            answers.push({answer:'b'});
        }
        if(data[0].answer3){
            answers.push({answer:'c'});
        }
        if(data[0].answer4){
            answers.push({answer:'d'});
        }

        $scope.answers = answers;
        $scope.data.mySelect= $scope.answers[0].answer;


    }).error(function(){
        alert("error");
    });
}
])



.controller('unansweredQuestionsCtrl', ['$scope', '$stateParams', '$http', '$state', 'factoryData', '$timeout', '$interval',// The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams, $http, $state,factoryData,$timeout,$interval) {
    $scope.isVisible = {};
    $scope.isVisible.visible=false;
    
    $scope.doRefresh = function() {
        
        $timeout( function() {
            var url = 'http://localhost:80/getUnansweredQuestions.php';
            $http.get(url).success(function(response) {
                // Store response data
                //$scope.users = response.data;
                var data = response;
                var numofQuestions=0;
                var Questions = [];
                var IDs = [];
           
                    for(var i in data){
                        var MyString = data[i].question.toString();
                        Questions.push({name:MyString,id:data[i].id});        
                    }
            
            
                    $scope.data = Questions;
            
                    if($scope.data[0].name){
                        $scope.isVisible.visible = false;
                        
                    }
                    else{
                        $scope.isVisible.visible = true;
                        
                    }
                
                    
                
                
              
            }).error(function(){
                alert("error");
            });
    
            //Stop the ion-refresher from spinning
            $scope.$broadcast('scroll.refreshComplete');
        
        }, 1000);
    };

    $scope.displayQuestion = function(id){
        factoryData.setId(id);
        
        $state.go('menu.answerQuestions');
    };


    $scope.load = function(){
        var url = 'http://localhost:80/getUnansweredQuestions.php';
        $http.get(url).success(function(response) {
            // Store response data
            //$scope.users = response.data;
            var data = response;
            var numofQuestions=0;
            var Questions = [];
            var IDs = [];
        
            
                for(var i in data){
                    var MyString = data[i].question.toString();
                    Questions.push({name:MyString,id:data[i].id});        
                
        
        
                    $scope.data = Questions;
                }
                if($scope.data[0].name){
                    $scope.isVisible.visible= false;
                    
                }else{
                    $scope.isVisible.visible= true;
                    
                }
            
            
    
            
          
          
        }).error(function(){
            alert("error");
        });
    }
    
    $timeout(function(){
        
        $scope.load();
      },1000);
      $scope.load();
      $interval($scope.load, 1000);
      
      

}])



   
.controller('questionsCtrl', ['$scope', '$stateParams', '$http', '$state', 'factoryData', '$timeout',// The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams, $http, $state,factoryData,$timeout) {

    
    $scope.doRefresh = function() {
        
        $timeout( function() {
            var url = 'http://localhost:80/getData.php';
            $http.get(url).success(function(response) {
                // Store response data
                //$scope.users = response.data;
                var data = response;
                var numofQuestions=0;
                var Questions = [];
                var IDs = [];
           
                for(var i in data){
                    var MyString = data[i].question.toString();
                    Questions.push({name:MyString,id:data[i].id});        
                }
        
        
                $scope.data = Questions;
              
              
            }).error(function(){
                alert("error");
            });
    
            //Stop the ion-refresher from spinning
            $scope.$broadcast('scroll.refreshComplete');
        
        }, 1000);
    };

    $scope.displayQuestion = function(id){
        factoryData.setId(id);
        
        $state.go('menu.question');
    };

    var url = 'http://localhost:80/getData.php';
    $http.get(url).success(function(response) {
        // Store response data
        //$scope.users = response.data;
        var data = response;
        var numofQuestions=0;
        var Questions = [];
        var IDs = [];
    
        for(var i in data){
            var MyString = data[i].question.toString();
            Questions.push({name:MyString,id:data[i].id});        
        }


            $scope.data = Questions;
      
      
    }).error(function(){
        alert("error");
    });
    

}])

.controller('questionCtrl', ['$scope', '$stateParams', 'factoryData', '$http', // The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams,factoryData,$http) {
    var no = factoryData.getId();
   

    var url = 'http://localhost:80/getOldQuestion.php';

    $http.post(url, {'id' : no}).success(function(response){

        var data = response;
        //alert(data[0].question);
        $scope.data = {Question:data[0].question,answer1:data[0].answer1,answer2:data[0].answer2,answer3:data[0].answer3,answer4:data[0].answer4,correct:data[0].correct};
        
        
    }).error(function(){
        alert("error");
    })

}])
   
.controller('menuCtrl', ['$scope', '$stateParams','$window','$state', // The following is the constructor function for this page's controller. See https://docs.angularjs.org/guide/controller
// You can include any angular dependencies as parameters for this function
// TIP: Access Route Parameters for your page via $stateParams.parameterName
function ($scope, $stateParams,$window,$state) {

    $scope.SignOut = function(){
        var conf = confirm("Are you sure you wish to Sign Out?");
        if(conf){
            $window.localStorage["saved"] = null;
            $state.go('login');
        }
        
        
    }
}])
 