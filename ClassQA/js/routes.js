angular.module('app.routes', [])

.config(function($stateProvider, $urlRouterProvider) {

  // Ionic uses AngularUI Router which uses the concept of states
  // Learn more here: https://github.com/angular-ui/ui-router
  // Set up the various states which the app can be in.
  // Each state's controller can be found in controllers.js
  $stateProvider
    

      .state('menu.home', {
    url: '/page1',
    views: {
      'side-menu21': {
        templateUrl: 'templates/home.html',
        controller: 'homeCtrl'
      }
    }
  })

  .state('menu.answerQuestions', {
    url: '/page2',
    views: {
      'side-menu21': {
        templateUrl: 'templates/answerQuestions.html',
        controller: 'answerQuestionsCtrl'
      }
    }
  })

  .state('menu.questions', {
    url: '/page3',
    views: {
      'side-menu21': {
        templateUrl: 'templates/questions.html',
        controller: 'questionsCtrl'
      }
    }
  })

  .state('menu', {
    url: '/side-menu21',
    templateUrl: 'templates/menu.html',
    controller: 'menuCtrl'
  })

$urlRouterProvider.otherwise('/side-menu21/page1')


});