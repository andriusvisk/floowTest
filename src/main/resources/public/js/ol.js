/**
 * Created by agnegv on 6/30/16.
 */
var map = new OpenLayers.Map(
    'map',
    {
        maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),
        // Lithuania only
        //restrictedExtent: new OpenLayers.Bounds(2200000, 7100000, 3000000, 7700000),
        maxResolution: 156543.0339,
        numZoomLevels: 19,
        units: 'm'
    }
);

var mapnik = new OpenLayers.Layer.OSM("OSM", null, {
    tileOptions: {crossOriginKeyword: null}
});

var wgsProjection = new OpenLayers.Projection("EPSG:4326"); // WGS 1984
var mercatorProjection = new OpenLayers.Projection("EPSG:900913"); // Spherical Mercator
var lksProjection = new OpenLayers.Projection("EPSG:3346");  // LKS 94

var wkt = new OpenLayers.Format.WKT();

var position = new OpenLayers.LonLat(21.2650, 55.6516).transform(wgsProjection, mercatorProjection);
var zoom = 11;

var openmaplt = new OpenLayers.Layer.OSM(
    "Openmap.lt",
    "http://osmlt.openmap.lt/${z}/${x}/${y}.png", {
        numZoomLevels: 26,
        alpha: true,
        isBaseLayer: true,
        tileOptions: {crossOriginKeyword: null}
    },
    "http://osmlt.openmap.lt"
);

var vectors = new OpenLayers.Layer.Vector("Vector Layer", {
    styleMap: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(OpenLayers.Util.applyDefaults({
            // labels
            //label: "${name}",
            fontColor: "${favColor}",
            fontSize: "18px",
            fontFamily: "Courier New, monospace",
            fontWeight: "bold"
            //labelAlign: "${align}"
            //labelXOffset: "${xOffset}",
            //labelYOffset: "${yOffset}"
        }, OpenLayers.Feature.Vector.style["default"])),
    })

});

function reorderLayers() {
    // keep layers ordered
    map.setLayerIndex(vectors, 5);
    map.setLayerIndex(kml, 10);
}


var kml = new OpenLayers.Layer.Vector("KML", {
    //rendererOptions: {zIndexing: true},
    projection: wgsProjection,
    strategies: [new OpenLayers.Strategy.BBOX()],
    protocol: new OpenLayers.Protocol.HTTP({
        url: "rs/geojson/1",
        format: new OpenLayers.Format.KML({
            extractStyles: true,
            extractAttributes: true,
            maxDepth: 2
        })
    }),
    styleMap: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(OpenLayers.Util.applyDefaults({
            externalGraphic: "http://openlayers.org/dev/img/marker-blue.png",
            graphicOpacity: 1,
            graphicHeight: 25,
            graphicWidth: 21,
            graphicXOffset: -12,
            graphicYOffset: -25
            //graphicZIndex: 800
        }, OpenLayers.Feature.Vector.style["default"])),
        "select": new OpenLayers.Style({
            //    externalGraphic: "http://openlayers.org/dev/img/marker-blue.png"
            //
        })
    })
});

kml.events.on({
    "featureselected": function (e) {
        e.feature.popup = new OpenLayers.Popup.FramedCloud("pop",
            e.feature.geometry.getBounds().getCenterLonLat(),
            null,
            '<div class="markerContent">' + e.feature.attributes.description + '</div>',
            null,
            true,
            function () {
                controls.selector.unselectAll();
            }
        );
        e.feature.popup.closeOnMove = true;
        map.addPopup(e.feature.popup);
    },
    "featureunselected": function (e) {
        e.feature.popup.destroy();
        e.feature.popup = null;
    }
});

vectors.events.on({
    "featureselected": function (e) {
        //console.log('Selected feature');
        //panel.activateControl(controls.modify);
    },
    "featureunselected": function (e) {
        //panel.activateControl(controls.selector);
    }
});

function createPopup(feature) {
    feature.popup = new OpenLayers.Popup.FramedCloud("pop",
        feature.geometry.getBounds().getCenterLonLat(),
        null,
        '<div class="markerContent">' + feature.attributes.description + '</div>',
        null,
        true,
        function () {
            kml_controls['selector'].unselectAll();
        }
    );
    //feature.popup.closeOnMove = true;
    map.addPopup(feature.popup);
}

function destroyPopup(feature) {
    feature.popup.destroy();
    feature.popup = null;
}
map.addLayers([openmaplt, mapnik, vectors, kml]);
map.zoomToMaxExtent();
map.addControl(new OpenLayers.Control.LayerSwitcher());

var panel = new OpenLayers.Control.Panel({
    'displayClass': 'olControlEditingToolbar'
});

var snapVertex = {methods: ['vertex', 'edge'], layers: [vectors]};

function polygonStart(feature) {
    //
}

function polygonAdded(feature) {
    feature2 = feature.clone();
    feature2.geometry.transform(mercatorProjection, wgsProjection);
    wkt_poly = wkt.write(feature2);
    delete feature2;

    var params = 'polygon=' + encodeURIComponent(wkt_poly) + '&' +
        'fid=' + encodeURIComponent(feature.id);

    PF('dlg').show();
    document.getElementById('dlgpolygon').value = encodeURIComponent(wkt_poly);
    document.getElementById('dlgfid').value = encodeURIComponent(feature.id);

    //var f = YAHOO.util.Dom.getAncestorByTagName("map", "form");
    //YAHOO.util.Connect.initHeader("ACCEPT", NAGARE_CONTENT_TYPE);
    //YAHOO.util.Connect.setForm(f, nagare_hasUpload(f));
    //YAHOO.util.Connect.asyncRequest("POST", "rs/geojson", null, params);
}

function polygonModified(feature) {
    feature2 = feature.clone();
    feature2.geometry.transform(mercatorProjection, wgsProjection);
    wkt_poly = wkt.write(feature2);
    delete feature2;

    var params = 'polygon=' + encodeURIComponent(wkt_poly) + '&' +
        'fid=' + encodeURIComponent(feature.id);

    //var f = YAHOO.util.Dom.getAncestorByTagName("map", "form");
    //YAHOO.util.Connect.initHeader("ACCEPT", NAGARE_CONTENT_TYPE);
    //YAHOO.util.Connect.setForm(f, nagare_hasUpload(f));
    YAHOO.util.Connect.asyncRequest("POST", "rs/geojson", nagare_callbacks, params);
}

var controls = {
//        point: new OpenLayers.Control.DrawFeature(vectors,
//                    OpenLayers.Handler.Point,
//                    {'displayClass': 'olControlDrawFeaturePoint',
//                     handlerOptions: snapVertex}),

//        line: new OpenLayers.Control.DrawFeature(vectors,
//                    OpenLayers.Handler.Path,
//                    {'displayClass': 'olControlDrawFeaturePath',
//                     handlerOptions: snapVertex}),

    polygon: new OpenLayers.Control.DrawFeature(vectors,
        OpenLayers.Handler.Polygon,
        {
            'displayClass': 'olControlDrawFeaturePolygon',
            handlerOptions: snapVertex,
            featureAdded: polygonAdded,
        }),

    modify: new OpenLayers.Control.ModifyFeature(vectors,
        {
            'displayClass': 'olControlDrawFeaturePoint',
            //standalone: true,
            //clickout: true,
            snappingOptions: snapVertex,
            onModificationStart: polygonStart,
            //onModification: onModification,
            onModificationEnd: polygonModified
        }),

    selector: new OpenLayers.Control.SelectFeature([vectors, kml], {
        clickout: false,
        toggle: false,
        multiple: false,
        hover: false,
        eventListeners: {
            featurehighlighted: function overlay_delete(event) {
                console.log('cia');
                var feature = event.feature;
                vectors.removeFeatures([feature]);
                // TODO other clean up - remove popups or send some ajax to server.  Up to you....

            }
        }
    })

};

// allow mouse panning over vector layers
if (typeof(controls['selector'].handlers) != 'undefined') { // OL 2.7
    controls['selector'].handlers.feature.stopDown = false;
} else if (typeof(controls['selector'].handler) != 'undefined') { // OL < 2.7
    controls['selector'].handler.stopDown = false;
    controls['selector'].handler.stopUp = false;
}

for (var key in controls) {
    panel.addControls([controls[key]]);
}

map.addControl(panel);
controls['polygon'].activate();

map.addControl(
    new OpenLayers.Control.MousePosition({
        id: 'wgs_mouse',
        prefix: 'WGS84: ',
        separator: '&nbsp;&nbsp;&nbsp;&nbsp;',
        numDigits: 4,
        emptyString: 'Mouse is not over map.',
        displayProjection: wgsProjection
    })
);

map.addControl(
    new OpenLayers.Control.MousePosition({
        id: 'lks_mouse',
        prefix: 'LKS94: ',
        separator: '&nbsp;&nbsp;&nbsp;&nbsp;',
        numDigits: 4,
        emptyString: 'Mouse is not over map.',
        displayProjection: lksProjection
    })
);

//kml.events.fallThrough = true;
//vectors.events.fallThrough = true;

panel.activateControl(controls.selector);

//map.setCenter(new OpenLayers.LonLat(0, 0), 3);

//var wkt = new OpenLayers.Format.WKT();
//var features = wkt.read("POLYGON((-3.69140625 -21.796875,-11.953125 -0.17578125,4.04296875 -2.98828125, 4 -15, 0 -20))");
//vectors.addFeatures(features);


//    map.events.register("mousemove", map, function(e) {
//        var position = this.events.getMousePosition(e);
//        OpenLayers.Util.getElement("coords").innerHTML = position;
//    });

map.div.oncontextmenu = function myContextMenu(e) {
    if (OpenLayers.Event.isRightClick(e)) {
        alert("Right button click"); // Add the right click menu here
        return false;
    }
};

map.setCenter(position, zoom);

var regions = [];
reorderLayers();

function pushPolygon(polygon, id) {
    region = wkt.read(polygon);
    region.id = id;
    region.geometry.transform(wgsProjection, mercatorProjection);
    region.attributes = {
        name: "Klaipėda-07",
        favColor: "purple",
        align: "lb",
        'displayClass': 'olControlDrawFeaturePoint'
    };
    regions.push(region);
}

function getJsonAllPolygons() {

    var url = "http://localhost:8080/erp_plus_t/rs/geojson";
    $.getJSON(url, function (data) {
        $.each(data, function (i, item) {
            pushPolygon(item.polygon, item.id);
        });
        vectors.addFeatures(regions);
        //reorderLayers();
    });
    //console.log(regions);
}