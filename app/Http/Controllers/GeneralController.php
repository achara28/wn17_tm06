<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Auth;
use App\Question;
use App\autoquestion;
use App\User;
use DB;
class GeneralController extends Controller
{

	public function index(){
		if (Auth::check() and strcmp(Auth::User()->role, "Instructor")==0 ){
			$questions = Question::all();
			return view('User.index',compact('questions'));
		}
		elseif  (Auth::check() and strcmp(Auth::User()->role, "Student")==0 )   {
			$questions = Question::all();
			return view('User.student_index',compact('questions'));
		}

		else{
			return view('Sessions.create');
		}
	}
	public function questionStore(){
		$this->validate(request(),[
			'question' => 'required',
			'answer1' => 'required',
			'answer2' => 'required',
			'answer3' => 'required',
			'answer4' => 'required',
			'correct' => 'required'
			]);

		Question::create([
			'question' => request('question'),
			'answer1' 	=> request('answer1'),
			'answer2' 	=> request('answer2'),
			'answer3' 	=> request('answer3'),
			'answer4' 	=> request('answer4'),
			'correct' 	=> request('correct')

			]);

		return redirect('/user');


	}

	public function questionUpdate($id){
		$this->validate(request(),[
			'question' => 'required',
			'answer1' => 'required',
			'answer2' => 'required',
			'answer3' => 'required',
			'answer4' => 'required',
			'correct' => 'required'
			]);
		$question = Question::find($id);
		$question->question = request('question');
		$question->answer1 = request('answer1');
		$question->answer2 = request('answer2');
		$question->answer3 = request('answer3');
		$question->answer4 = request('answer4');
		$question->correct = request('correct');
		$question->save();
		return redirect('/user');
	}
	public function deletequestion($id){

		$question = Question::find($id);

		$question->delete();
		return back();
	}

	

	public function questionToAnswer($id){
		//$ms=round(microtime(true)*1000);

		$question = Question::find($id);
		$que = autoquestion::get()[0];
		$que->question = $question->question;
		$que->answer1  	= $question->answer1;
		$que->answer2  	= $question->answer2;
		$que->answer3  	= $question->answer3;
		$que->answer4  	= $question->answer4;
		$que->correct  	= $question->correct;
		$que->no1   	= 0;
		$que->no2   	= 0;
		$que->no3   	= 0;
		$que->no4   	= 0;		
		$que->save();
		$question->anleo = 1;
		$question->save();

		//return response()->json($que);

	}


	public function rojastension(){
		$que = autoquestion::get()[0];
		$que->question = "";
		$que->save();
		$users = User::all();
		foreach ($users as $user) {
			$user->answered=0;
			$user->save();
		}
	}
	public function statstension($id){
		$question = Question::find($id);
		$que = autoquestion::get()[0];
		$question->no1 = $que->no1;
		$question->no2 = $que->no2;
		$question->no3 = $que->no3;
		$question->no4 = $que->no4;
		if (strcmp($question->correct,"answer1" )==0) {
			$question->cor= $que->no1;
		}
		else if (strcmp($question->correct,"answer2" )==0) {
			$question->cor= $que->no2;
		}
		else if (strcmp($question->correct,"answer3" )==0) {
			$question->cor= $que->no3;
		}
		else if (strcmp($question->correct,"answer4" )==0) {
			$question->cor= $que->no4;
		}

		$question->save();

	}

	public function inye(){
		$que = autoquestion::get()[0];
		$temp=$que->no1;
		$temp=$temp+1;
		$que->no1=$temp;
		$que->save();
		Auth::User()->answered=1;
		Auth::User()->save();
		return redirect('/user');	
	}

	public function inbl(){
		$que = autoquestion::get()[0];
		$temp=$que->no2;
		$temp=$temp+1;
		$que->no2=$temp;
		$que->save();
		Auth::User()->answered=1;
		Auth::User()->save();
		return redirect('/user');	
	}

	public function inre(){
		$que = autoquestion::get()[0];
		$temp=$que->no3;
		$temp=$temp+1;
		$que->no3=$temp;
		$que->save();
		Auth::User()->answered=1;
		Auth::User()->save();
		return redirect('/user');	
	}

	public function ingr(){
		$que = autoquestion::get()[0];
		$temp=$que->no4;
		$temp=$temp+1;
		$que->no4=$temp;
		$que->save();
		Auth::User()->answered=1;
		Auth::User()->save();
		return redirect('/user');	
	}


}




