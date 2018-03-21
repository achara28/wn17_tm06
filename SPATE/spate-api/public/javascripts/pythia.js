/**
 * Created by canast02 on 6/4/15.
 */
$(document).ready(function() {

    var BASE_URL = "";

    $("#logout-btn").on('click', function(e) {
        $.ajax({
            type: "DELETE",
            url: BASE_URL + "/user/logout",
            success: function(data) {
                //if(data.status == "success") {
                    window.location.href = "/login";
               // }
            }
        })
    });

    var gsm=  $('#gms-panel-body');
    gsm.css("visibility", "hidden");
    gsm.css("height", "0");


    $('#gms-collapse').click(function(evt) {
        var isHidden = gsm.css("visibility") == "hidden";
        if(!isHidden){
            gsm.css("visibility", "hidden");
            gsm.css("height", "0");
        }else {
            gsm.css("visibility", "visible");
            gsm.height($("#page-wrapper").height());
        }
    });


    var umts=  $('#umts-panel-body');
    umts.css("visibility", "hidden");
    umts.css("height", "0");

    $('#umts-collapse').click(function(evt) {
        var isHidden = umts.css("visibility") == "hidden";
        if(!isHidden){
            umts.css("visibility", "hidden");
            umts.css("height", "0");
        }else {
            umts.css("visibility", "visible");
            umts.height($("#page-wrapper").height());
        }
    });

    $(window).resize(function(){
        var isHidden = gsm.css("visibility") == "hidden";
        if(!isHidden) {
            $('#gms-panel-body').height($("#page-wrapper").height());
        }

        var isHidden = umts.css("visibility") == "hidden";
        if(!isHidden) {
            $('#umts-panel-body').height($("#page-wrapper").height());
        }
    });
});