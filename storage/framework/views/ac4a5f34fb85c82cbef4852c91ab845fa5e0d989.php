<?php $__env->startSection('content'); ?>


<style type="text/css">
#btnShow,
#btnSearch,
#btnClear{
	display: inline-block;
	vertical-align: top;
}


</style>

<script type="text/javascript">

	function start(){
		var fiveMinutes = 27* 1;
		display = document.querySelector('#time');
		startTimer(fiveMinutes, display);

	}
	function startTimer(duration, display) {
		var timer = duration, minutes, seconds;
		var downloadtimer=setInterval(function () {
			minutes = parseInt(timer / 60, 10)
			seconds = parseInt(timer % 60, 10);

			minutes = minutes < 10 ? "0" + minutes : minutes;
			seconds = seconds < 10 ? "0" + seconds : seconds;

			display.textContent = minutes + ":" + seconds;

			if (--timer < 0) {
				timer = duration;
				clearInterval(downloadtimer)
			}
		}, 1000);

	}







</script>









<br><br><br><br><br><br>
<div  class="container">
	
	<!-- Modal -->
	<div class="modal fade" id="myModalhistory" role="dialog">
		<div class="modal-dialog">

			<!-- Modal content-->
			<div class="modal-content" style="color: #4c4c4c;">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h2 class="modal-title">Questions History</h2>
				</div>
				<div class="modal-body">
					<?php $__currentLoopData = $questions; $__env->addLoop($__currentLoopData); foreach($__currentLoopData as $question): $__env->incrementLoopIndices(); $loop = $__env->getLastLoop(); ?>
					<?php if($question->anleo): ?>
					<h4><strong> Question : <span style="color: #4c4c4c;"><?php echo e($question->question); ?> </span> </strong></h4>

					<p><strong>Answer1 : </strong> <span style="color: #f9b754"><?php echo e($question->answer1); ?></span></p>
					<p><strong>Answer2 : </strong> <span style="color: #296cc6" ><?php echo e($question->answer2); ?></span></p>
					<p><strong>Answer3 : </strong> <span style="color: #ce3d3d" ><?php echo e($question->answer3); ?></span></p>
					<p><strong>Answer4 : </strong> <span style="color: #60a850" ><?php echo e($question->answer4); ?></span></p>
					<br>
					<p><strong>Correct Answer : </strong><?php echo e($question->correct); ?></p>


					<?php endif; ?>	
					<hr>
					<?php endforeach; $__env->popLoop(); $loop = $__env->getLastLoop(); ?>



				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				</div>
			</div>

		</div>
	</div>



	<div style="text-align: center;">
		<button type="button" class="btn btn-info btn-lg" data-toggle="modal" data-target="#myModalhistory">History</button>
		<br>
		<br>
		<br>
		<button style="visibility: hidden;" type="button" id="alokozay"  class="btn btn-warning btn-lg" data-toggle="modal" data-target="#myModal">Answer the question</button>
	</div>
	<div class="row">
		<div class="col-sm-4"></div>
		<div class="col-sm-8">


			<div class="modal fade" id="myModal" role="dialog">
				<div class="modal-dialog">

					<!-- Modal content-->
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title">Question</h4> <br> 
							<p>You have about <strong id="time"></strong> seconds</p>
						</div>
						<div class="modal-body">

							<h3 style="font-size:20px; color: #4c4c4c; " id="question" ></h3> 
							<hr>
							<strong><p style="color: #f9b754;" id="answer1"></p></strong>
							<strong><p style="color: #296cc6;" id="answer2"></p></strong>
							<strong><p style="color: #ce3d3d;" id="answer3"></p></strong>
							<strong><p style="color: #60a850;" id="answer4"></p></strong>
							<hr>

							<h3>Answer the question </h3>
							<br>
							<form method="post"  id="btnClear" action="/inye" style="text-align: center;" >
								<?php echo e(csrf_field()); ?>  
								<button Style="width: 60px; height:40px;" type="submit" class="btn btn-warning"  onclick="answer()"></button>
							</form>
							<form method="post"  id="btnClear" action="inbl" style="text-align: center;">
								<?php echo e(csrf_field()); ?>  
								<button Style="width: 60px; height:40px;" type="submit" class="btn btn-primary"></button>
							</form>
							<form method="post"  id="btnClear" action="inre" style="text-align: center;">
								<?php echo e(csrf_field()); ?>  
								<button Style="width: 60px; height:40px;" type="submit" class="btn btn-danger"></button>
							</form>
							<form method="post"  id="btnClear" action="ingr" style="text-align: center;">
								<?php echo e(csrf_field()); ?>  
								<button Style="width: 60px; height:40px;" type="submit" class="btn btn-success"></button>
							</form>

						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
						</div>
					</div>

				</div>
			</div>




		</div>

	</div>
</div>

<script type="text/javascript">
	var x=0;
	function updatePost() {
		
		setInterval(function() {
			$.ajax({
				type: "GET",
            url: "/studentapt", // You add the id of the post and the update datetime to the url as well
            success: function(response) {
                // If not false, update the post
                if (response) {
                    // Update the h2 with the new title from the post
                    
                    if (response.question && response.answered!=1 ){
                    	if (x==0){
                    		start()
                    	}
                    	x++;
                    	document.getElementById("alokozay").style.visibility="visible"
                    	
                    	$("#question").text(response.question);
                    	$("#answer1").text(response.answer1);
                    	$("#answer2").text(response.answer2);
                    	$("#answer3").text(response.answer3);
                    	$("#answer4").text(response.answer4);
                    	
                    }
                    else{
                    	//location.reload();
                    	
                    	$(function () {
                    		$('#myModal').modal('hide');
                    	});
                    	document.getElementById("alokozay").style.visibility="hidden"
                    	x=0;

                    }
                    // Update the div with the new body from the post
                         //   $("#article > .body").html(response.body);
                     }
                     else {
                     	
                     	document.getElementById("alokozay").style.visibility="hidden"
                     }

                 }
             });
    }, 1000); // Do this every 5 seconds
	}


	updatePost();
</script>














<?php $__env->stopSection(); ?>
<?php echo $__env->make('layouts.master', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>