/**
 * Created by canast02 on 31/3/15.
 */
$(document).ready(function() {

    $("#login-btn").on('click', function(e) {
        e.preventDefault();

        hideError();

        var username = $("input[name=username]").val();
        var password = $("input[name=password]").val();

        if(username == "" || password == "") {
            $("#error-field").removeClass("hidden");
            $("#error-message").text("username/password should not be empty");
        }
        else {
            password = Sha1.hash(password);
            console.log(username, password);

            var credentials = {
                username: username,
                password: password
            };

            $.ajax({
                type: "POST",
                url: "/user/login",
                contentType: "application/json",
                data: JSON.stringify(credentials),
                success: function(data) {
                    console.log(data);
                    if(data.status != "success") {
                        showError(data.message);
                    }
                    else {
                        if(data.role=="admin") {
                            setCookie("Authorization", data.token.authToken, 1);
                            setTimeout(function() {
                                window.location.href = "/";
                            }, 50);
                        }
                        else {
                            showError("not enough privileges");
                        }
                    }
                },
                error: function(data) {
                    console.log("error", data);
                }
            });
        }
    });

    var showError = function(errorMsg) {
        $("#error-field").removeClass("hidden");
        $("#error-message").text(errorMsg);
    };

    var hideError = function() {
        $("#error-field").addClass("hidden");
    };
});

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}