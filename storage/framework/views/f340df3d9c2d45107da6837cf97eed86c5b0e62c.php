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

	function start(id,sum){

		$(function(){
			$("button.luciano").attr("disabled", true);
			$("button.lucianoedit").attr("disabled", true);
			$("button.lucianodelete").attr("disabled", true);	
			$('#addbutton').attr('disabled','disabled');
		});
		document.getElementById("qid").innerHTML=sum;


		//document.getElementsByClassName("luciano").disabled=true;
		
		updatePost(id)
		document.getElementById("caruso").style.visibility="visible";
		
		var fiveMinutes = 15* 1;
		display = document.querySelector('#time');
		startTimer(fiveMinutes, display,id);
		

	}

	function updatePost(id) {
		
		$.ajax({
			  url: "/question/" + id, // You add the id of the post and the update datetime to the url as well
			  headers: {
			  	'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
			  },
			  type: "POST"

			});
	}


	function rojas() {
		//await sleep(15000)
		$.ajax({
			  url: "/rojascontinue/", // You add the id of the post and the update datetime to the url as well
			  type: "GET"

			});
	}
	function stats(id) {
		//await sleep(15000)
		$.ajax({
			  url: "/statscontinue/" + id, // You add the id of the post and the update datetime to the url as well
			  headers: {
			  	'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
			  },
			  type: "POST"

			});
	}



	function startTimer(duration, display,id) {
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
				rojas()
				stats(id)
				location.reload();



			}
		}, 1000);

	}


</script>


<div  class="container" style="font-family:'ComicSans MS', cursive, sans-serif">
	

	<?php $sum =0; ?>

	<div id="caruso"  style="visibility:hidden; font-size:20px; color:#4c4c4c;">Students have <strong><span id="time"> 00:15</span></strong> seconds to answer the question <strong><span id="qid"></span></strong></div>

	<h2 style="color: #4c4c4c;">Questions</h2>
	<hr>
	<div class="container" style="overflow-y:scroll; height: 100%; max-height: 500px;">
		<?php $__currentLoopData = $questions; $__env->addLoop($__currentLoopData); foreach($__currentLoopData as $question): $__env->incrementLoopIndices(); $loop = $__env->getLastLoop(); ?>
		<?php $sum++; ?>
		<div class="row">
			<div class="col-sm-5" style="border-right: solid 1px; border-right-color:#f2f2f2; word-wrap: break-word; ">
				<h3>  Question <?php echo e($sum); ?>   </h3>
				<br> <br>
				<p style="font-size: 17px"><?php echo e($question->question); ?> </p>
				<br> <br> 


				<div class="modal fade" id="modal<?php echo e($sum); ?>" role="dialog">
					<div class="modal-dialog">

						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" style="color: #4c4c4c;" >Edit your question</h4>
							</div>
							<div class="modal-body">
								<form method="POST" action="/user/updatequestion/<?php echo e($question->id); ?>">
									<?php echo e(csrf_field()); ?>

									<div class="form-group">
										<label style="font-size:20px; color: #4c4c4c;" for ="name">Question</label>
										<input type="text" class="form-control" id="question" name="question" value="<?php echo e($question->question); ?>">
									</div>
									<hr>
									<div class="form-group">
										<label for ="name" style="color: #f9b754;">Possible Answer 1</label>
										<input type="text" class="form-control" id="answer1" name="answer1" required="true" value="<?php echo e($question->answer1); ?>">
										<label for ="name" style="color: #296cc6;">Possible Answer 2</label>
										<input type="text" class="form-control" id="answer2" name="answer2" value="<?php echo e($question->answer2); ?>">
										<label for ="name" style="color: #ce3d3d;">Possible Answer 3</label>
										<input type="text" class="form-control" id="answer3" name="answer3" value="<?php echo e($question->answer3); ?>">
										<label for ="name" style="color: #60a850;">Possible Answer 4</label>
										<input type="text" class="form-control" id="answer4" name="answer4" value="<?php echo e($question->answer4); ?>">

									</div>
									<hr>

									<div class="form-group">
										<label for ="name" style="font-size:20px; color: #4c4c4c;">Choose the correct answer </label> 
										<br> <br>
										<input id="corr1<?php echo e($sum); ?>" type="radio" name="correct" value="answer1" required > <span style="color:#f9b754;" >Answer 1</span> <br>
										<input  id="corr2<?php echo e($sum); ?>" type="radio" name="correct" value="answer2" required> <span style="color:#296cc6;" >Answer 2</span> <br>
										<input  id="corr3<?php echo e($sum); ?>" type="radio" name="correct" value="answer3" required> <span style="color:#ce3d3d;" >Answer 3</span> <br>
										<input  id="corr4<?php echo e($sum); ?>" type="radio" name="correct" value="answer4" required> <span style="color:#60a850;" >Answer 4</span> <br>
									</div>
									<br>






									<div class="form-group">
										<button  type="submit" class="btn btn-default">Update</button> 
									</div>

									<?php echo $__env->make('layouts.errors', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>			
								</form>
							</div>
							<div class="modal-footer">
								<button type="submit" class="btn btn-default" data-dismiss="modal">Close</button>
							</div>
						</div>

					</div>
				</div>

				<script type="text/javascript">

					var corr= "<?php echo e($question->correct); ?>";

					if (corr==="answer1")
						document.getElementById("corr1<?php echo e($sum); ?>").checked = true;
					else if (corr==="answer2")
						document.getElementById("corr2<?php echo e($sum); ?>").checked = true;
					else if ((corr==="answer3"))
						document.getElementById("corr3<?php echo e($sum); ?>").checked = true;
					else
						document.getElementById("corr4<?php echo e($sum); ?>").checked = true;
				</script>


				<div class="row">
					<div class="col-sm-12 ">

						<button type="button" id="btnSearch"
						class="btn btn-warning btn-md center-block lucianoedit" data-toggle="modal" data-target="#modal<?php echo e($sum); ?> " Style="width: 100px;" OnClick="btnSearch_Click" >Edit</button> 




						<form method="post"  id="btnClear" action="/question/delete/<?php echo e($question->id); ?>" style="text-align: center;">
							<?php echo e(csrf_field()); ?>  
							<?php echo e(method_field('DELETE')); ?>

							<button Style="width: 100px;" type="submit" class="btn btn-danger lucianodelete">Delete</button>
						</form>

					<button Style="width: 100px;" type="submit" onclick="start(<?php echo e($question->id); ?>,<?php echo e($sum); ?> )" class="luciano btn btn-success">Project</button>
					
				</div>
			</div>
		</div>
		<div style="color: #4c4c4c" class="col-sm-7">
			<?php if(($question->anleo)): ?>
			<h3>Answers </h3>		
			<p> <strong style="color: #f9b754"> Answer 1 </strong> : <span style="font-size: 17px"><?php echo e($question->no1); ?></span></p>
			<p><strong style="color: #296cc6"> Answer 2 </strong> : <span style="font-size: 17px"><?php echo e($question->no2); ?></span></p>
			<p><strong style="color: #ce3d3d"> Answer 3 </strong> : <span style="font-size: 17px"><?php echo e($question->no3); ?></span></p>
			<p><strong style="color: #60a850"> Answer 4 </strong> : <span style="font-size: 17px"><?php echo e($question->no4); ?></span></p>
			<p><strong style="color:"> Correct Answer </strong> : <span style="font-size: 17px"><?php echo e($question->cor); ?></span></p>


			<?php else: ?>
<br> <br>
				<h3 style="text-align: left;"> <strong><i>Not Projected Yet :)</i></strong></h3>
			<?php endif; ?>
		</div>

	</div>
	<hr>



	<?php endforeach; $__env->popLoop(); $loop = $__env->getLastLoop(); ?>
</div>

<hr>
<!-- Trigger the modal with a button -->
<button id="addbutton" type="button" class="btn btn-info btn-lg" data-toggle="modal" data-target="#myModal">Add</button>
<br>
<br>
<br>
<br>
<br>

<!-- Modal -->
<div class="modal fade" id="myModal" role="dialog">
	<div class="modal-dialog">

		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title" style="color: #4c4c4c;" >Insert your question</h4>
			</div>
			<div class="modal-body">
				<form method="POST" action="/user/createquestion">
					<?php echo e(csrf_field()); ?>

					<div class="form-group">
						<label style="font-size:20px; color: #4c4c4c;" for ="name">Question</label>
						<input type="text" class="form-control" id="question" name="question">
					</div>
					<hr>
					<div class="form-group">
						<label for ="name" style="color: #f9b754;">Possible Answer 1</label>
						<input type="text" class="form-control" id="answer1" name="answer1" required="true">
						<label for ="name" style="color: #296cc6;">Possible Answer 2</label>
						<input type="text" class="form-control" id="answer2" name="answer2">
						<label for ="name" style="color: #ce3d3d;">Possible Answer 3</label>
						<input type="text" class="form-control" id="answer3" name="answer3">
						<label for ="name" style="color: #60a850;">Possible Answer 4</label>
						<input type="text" class="form-control" id="answer4" name="answer4">

					</div>
					<hr>

					<div class="form-group">
						<label for ="name" style="font-size:20px; color: #4c4c4c;">Choose the correct answer </label> 
						<br> <br>
						<input  type="radio" name="correct" value="answer1" required > <span style="color:#f9b754;" >Answer 1</span> <br>
						<input  type="radio" name="correct" value="answer2" required> <span style="color:#296cc6;" >Answer 2</span> <br>
						<input  type="radio" name="correct" value="answer3" required> <span style="color:#ce3d3d;" >Answer 3</span> <br>
						<input  type="radio" name="correct" value="answer4" required> <span style="color:#60a850;" >Answer 4</span> <br>
					</div>
					<br>

					<div class="form-group">
						<button  type="submit" class="btn btn-default">Insert</button> 
					</div>

					<?php echo $__env->make('layouts.errors', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>			
				</form>
			</div>
			<div class="modal-footer">
				<button type="submit" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>

	</div>
</div>

</div>




<?php $__env->stopSection(); ?>
<?php echo $__env->make('layouts.master', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>