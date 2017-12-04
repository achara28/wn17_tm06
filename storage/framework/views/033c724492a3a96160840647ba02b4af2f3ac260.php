<!DOCTYPE html>
<html>
<head>
	<title></title>
	<link rel="stylesheet" type="text/css" href="<?php echo e(asset ('css/style.css')); ?>">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
	
</head>
<body>
	<div class="container text-center" style="margin-top: 7%;">   
		<div>
			<h1>Registration</h1>

			<form action="/register" method="post"  class="form-signin"> 
				<?php echo e(csrf_field()); ?>      
				<h3 class="form-signin-heading">Welcome! Please Register :)</h3>
				<hr class="colorgraph"><br>
				
				<div class="form-group">
					<label for="name"> Name: </label>
					<input type="text" class="form-control" id="name" name="name" required >
				</div>
				
				<div class="form-group">
					<label for="email"> Email: </label>
					<input type="email" class="form-control" id="email" name="email" required >
				</div>
				
				<div class="form-group">
					<label for="password"> Password:</label>
					<input type="password" class="form-control" id="password" name="password" required  >
				</div>
				<div class="form-group">
					<label for="password_confirmation"> Password Confirmation:</label>
					<input type="password" class="form-control" id="password_confirmation" name="password_confirmation" required >
				</div>

				<button class="btn btn-lg btn-danger btn-block" type="Submit"> Register </button>
				<br>
				<?php echo $__env->make('layouts.errors', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>	
			</form>
			

			<br>
			
		</div>

	</body>
	</html>
