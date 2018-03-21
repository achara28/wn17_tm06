/**
 * Created by costantinos on 15/2/2016.
 */
bootstrap_alert = function () {
}
bootstrap_alert.warning = function (message) {
    console.log(message);
    //$('#alert_placeholder').hide();
    //$('#alert_placeholder').html('<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + message + '</span></div>')
    //$('#alert_placeholder').alert();
    //$('#alert_placeholder').fadeTo(2000, 500).slideUp(500, function () {
    //    $('#alert_placeholder').alert('close');
    //});
};

bootstrap_alert.error = function (message) {
    $('#alert_placeholder').hide();
    $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + message + '</span></div>')
    $('#alert_placeholder').alert();
    $('#alert_placeholder').fadeTo(2000, 500).slideUp(500, function () {
        $('#alert_placeholder').alert('close');
    });
};

bootstrap_alert.success = function (message) {
    $('#alert_placeholder').hide();
    $('#alert_placeholder').html('<div class="alert alert-success alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + message + '</span></div>')
    $('#alert_placeholder').alert();
    $('#alert_placeholder').fadeTo(2000, 500).slideUp(500, function () {
        $('#alert_placeholder').alert('close');
    });
};


L.Control.Checkbox = L.Control.extend({
    options: {
        position: 'topright'
    },
    onAdd: function (map) {
        // create the control container with a particular class name
        var container = L.DomUtil.create('div', '');
        container.innerHTML = '<input type="checkbox"  name=' + this.options.name + ' data-label-text=' + this.options.text + '>';
        // ... initialize other DOM elements, add listeners, etc.
        container.setAttribute("tdata-toggle", "tooltip");
        container.setAttribute("data-placement", "bottom");
        container.setAttribute("title", this.options.title);

        return container;
    }
});

var checkbox = function (options) {
    return new L.Control.Checkbox(options)
};


L.Control.LiveButton = L.Control.extend({
    options: {
        position: 'topright'
    },
    onAdd: function (map) {
        // create the control container with a particular class name
        var container = L.DomUtil.create('div', '');

        container.innerHTML = '<div><sparn class="label live" > ' + this.options.text + '</sparn><button class="label btn btn-info" id="activequery"></button><div id="time" class="label bg-primary"></div></div>';
        // ... initialize other DOM elements, add listeners, etc.
        container.setAttribute("tdata-toggle", "tooltip");
        container.setAttribute("data-placement", "bottom");
        container.setAttribute("title", this.options.title);

        //data-toggle="tooltip" data-placement="left" title="When “Live” is green, presented results are based on measurements collected during the last 30 minutes."
        return container;
    }
});

var livebutton = function (options) {
    return new L.Control.LiveButton(options)
};
