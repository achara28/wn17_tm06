<!DOCTYPE html>
<html>
<head>
	<title></title>
	<link rel="stylesheet" type="text/css" href="<?php echo e(asset ('css/style.css')); ?>">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	
</head>
<body>
<div class="container text-center" style="margin-top: 7%;">   
		<div>
			<h1>Login</h1>
			<form action="/login" method="post" class="form-signin">  
				<?php echo e(csrf_field()); ?>       
				<h3 class="form-signin-heading">Welcome Back! Please Sign In</h3>
				<hr class="colorgraph"><br>

				<input type="email" class="form-control" id="email" name="email" placeholder="Email" autofocus="" /> <br>
				<input type="password" class="form-control" id= "password" name="password" placeholder="password" />     		  

				<button class="btn btn-lg btn-primary btn-block"  name="Submit" value="Login" type="Submit">Login</button> <br>
				<a href="/register">Register</a> 	
				<br>
				<?php echo $__env->make('layouts.errors', array_except(get_defined_vars(), array('__data', '__path')))->render(); ?>			
			</form>	

		</div>
	</div>

</body>
</html>