<?php

namespace App;

use Illuminate\Database\Eloquent\Model;


class Question extends Model
{
	public $timestamps = false;
    protected $fillable = ['question','answer1','answer2','answer3','answer4','correct','no1','no2','no3','no4','cor','anleo'];

public function user(){
		return $this->belongsTo(Question::class);
	}

}
