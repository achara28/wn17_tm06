<link rel="stylesheet" type="text/css" href="{{asset ('css/nav.css')}}">
<nav class="navbar navbar-default">
	<div class="container">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse-2">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/"><img src="{{ asset('img/logo.png') }}" >
			</a>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse" id="navbar-collapse-2">
			
			<ul class="nav navbar-nav">
{{-- 				<li class="{{ Request::segment(1) === 'home' ? 'active' : null }}">
				<a href="{{ url('home' )}}" ></i> home</a>
				</li> --}}
		
			{{-- 	<li><a href="#">About</a></li> --}}
			
			</ul>
			
			<ul class="nav navbar-nav navbar-right">

				@if (Auth::check())
				<li><a href="/user">{{Auth::user()->name }}</a></li>
				@endif

				@if (Auth::check())
				<li>
					<a class="btn btn-default btn-outline btn-circle"  href="/logout"  >Log Out</a>
				</li>
				@endif
				
				@if (! Auth::check())
				<li>
					<a class="btn btn-default btn-outline btn-circle"  href="/login"  >Log In</a>
				</li>
				@endif
			</ul>
			<div class="collapse nav navbar-nav nav-collapse" id="nav-collapse2">
				<form class="navbar-form navbar-right form-inline" role="form">
					<div class="form-group">
						<label class="sr-only" for="Email">Email</label>
						<input type="email" class="form-control" id="Email" placeholder="Email" autofocus required />
					</div>
					<div class="form-group">
						<label class="sr-only" for="Password">Password</label>
						<input type="password" class="form-control" id="Password" placeholder="Password" required />
					</div>
					<button type="submit" class="btn btn-success">Sign in</button>
				</form>
			</div>
		</div><!-- /.navbar-collapse -->
	</div><!-- /.container -->
</nav><!-- /.navbar -->

{{-- <nav class="navbar navbar-inverse">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>                        
			</button>
			<a class="navbar-brand" href="#">Clean Email</a>
		</div>
		<div class="collapse navbar-collapse" id="myNavbar">
			<ul class="nav navbar-nav">
				<li class="active"><a href="/">Home</a></li>
				<li><a href="#">About</a></li>
				<li><a href="#">Projects</a></li>
				<li><a href="#">Contact</a></li>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li><a href="/login"><span class="glyphicon glyphicon-log-in"></span> Login</a></li>
			</ul>
		</div>
	</div>
</nav> --}}