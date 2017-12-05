<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class autoquestion extends Model
{
	public $timestamps = false;
    protected $fillable = ['question','answer1','answer2','answer3','answer4','correct','no1','no2','no3','no4'];

    //
}
