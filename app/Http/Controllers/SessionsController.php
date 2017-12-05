<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\autoquestion; 
use Auth;

class SessionsController extends Controller
{
	
		public function create(){
		return view('sessions.create');
	}
	Public function store(){
		//attemt to authenticate the user
		if(! auth()->attempt(request(['email','password']))){
			return back()->withErrors([
				'message' => 'please check your credentials and try again.'
				]);
		}
		//if so sign them in

		return redirect('user');
		//redirect to the home page
	}
	
public function updated()
	{
		$question = autoquestion::get()[0];
		$question['answered']=Auth::User()->answered;
		return $question;
	}


	public function destroy(){
		auth()->logout();
		return redirect()->home();
	}
}