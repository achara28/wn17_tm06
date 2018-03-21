/**
 * Created by canast02 on 9/4/15.
 */

var PythiaClusterInfoControl = L.Control.extend({
    options: {
        position: 'bottomright'
    },

    clusterInfo: null,
    isInitialized: false,

    onAdd: function (map) {
        if(!this.isInitialized) {
            L.DomEvent
                .addListener(this.clusterInfo, 'click', L.DomEvent.stopPropagation)
                .addListener(this.clusterInfo, 'click', L.DomEvent.preventDefault)
                .addListener(this.clusterInfo, 'dblclick', L.DomEvent.stopPropagation)
                .addListener(this.clusterInfo, 'dblclick', L.DomEvent.preventDefault);
            this.isInitialized = true;
        }
        this.clusterInfo.reset();
        return this.clusterInfo;
    },

    show: function() {
        if(!this.clusterInfo.isVisible) {
            this.clusterInfo.isVisible = true;
        }
    },
    hide: function() {
        if(this.clusterInfo.isVisible) {
            this.clusterInfo.isVisible = false;
        }
    },

    setStatsObj: function(obj) {
        this.clusterInfo.statsObj = obj;
    },

    getStatsObj: function() {
        return this.clusterInfo.statsObj;
    }
});