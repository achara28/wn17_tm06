<!DOCTYPE html>
<html>
<head>
	

<meta name="csrf-token" content="{{ csrf_token() }}">
<meta name="viewport" content="width=device-width, initial-scale=1">


	<title>Internet Technologies</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

	<style type="text/css">
		footer {
			background-color: #555;
			color: white;
			padding: 15px;
		}
	</style>
</head>
<body>
	@include ('layouts.nav')
	@yield ('content')
	@include ('layouts.footer')

</body>
</html>