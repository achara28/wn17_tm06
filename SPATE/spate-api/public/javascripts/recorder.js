/**
 * Created by canast02 on 7/5/15.
 */
$("#toggle-recorder-btn").click(function(e) {
    var self = $(this);
    var url = "";
    var newText = "";
    self.attr('disabled','disabled');
    if(self.text()=="Start") {
        url = "/api/recorder/start";
        newText = "Stop";
    }
    else {
        url = "/api/recorder/stop";
        newText = "Start";
    }

    $.ajax({
        type: "GET",
        url: url,
        success: function(data) {
            self.text(newText);
            refresh();
            self.removeAttr('disabled');
        },
        error: refresh
    });
});

function refresh() {
    location.reload();
}