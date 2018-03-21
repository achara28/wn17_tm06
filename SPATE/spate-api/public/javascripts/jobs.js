/**
 * Created by canast02 on 6/4/15.
 */
$(document).ready(function() {
    $('#dataTables-runningJobs').DataTable({
        responsive: true
    });
    $('#dataTables-completedJobs').DataTable({
        responsive: true
    });
});

function requestDeleteJob(jobId) {
    $.ajax({
        type: 'DELETE',
        url: '/api/job/' + jobId,
        success: function(data) {
            console.log("deleted job: ", jobId);
            $("#"+jobId).hide();
        }
    });
}