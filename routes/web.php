<?php
Route::get ('/', 'GeneralController@index')-> name('home');
Route::get ('/user', 'GeneralController@index');

Route::get ('/register', 'RegistrationController@create');
Route::post ('/register', 'RegistrationController@store');

Route::get ('/logout','SessionsController@destroy');
Route::get ('/login', 'SessionsController@create');
Route::post ('/login', 'SessionsController@store');

Route::post ('/user/createquestion', 'GeneralController@questionStore');
Route::post ('/user/updatequestion/{question}', 'GeneralController@questionUpdate');
Route::delete('/question/delete/{question}','GeneralController@deletequestion');
Route::get('/studentapt', 'SessionsController@updated');

Route::post ('/question/{question}', 'GeneralController@questionToAnswer');
Route::get ('/rojascontinue', 'GeneralController@rojastension');
Route::post ('/statscontinue/{question}', 'GeneralController@statstension');

Route::post ('/inye', 'GeneralController@inye');
Route::post ('/inbl', 'GeneralController@inbl');
Route::post ('/inre', 'GeneralController@inre');
Route::post ('/ingr', 'GeneralController@ingr');