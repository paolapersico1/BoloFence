var style_geofence_walk = function(feature, resolution){
		var maxValue = max_intensity_walk;
    var value = feature.get("intensity");

    if (value >= 0 && value <= maxValue*0.1) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(247,251,255,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.1 && value <= maxValue*0.2) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(226,238,249,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.2 && value <= maxValue*0.3) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(205,224,242,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.3 && value <= maxValue*0.4) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(176,210,232,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.4 && value <= maxValue*0.5) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(137,191,221,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.5 && value <= maxValue*0.6) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(96,166,210,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.6 && value <= maxValue*0.7) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(62,142,196,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.7 && value <= maxValue*0.8) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(33,114,182,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.8 && value <= maxValue*0.9) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(10,84,158,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    } else if (value > maxValue*0.9) {
            style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: 'rgba(38,89,128,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 3}),fill: new ol.style.Fill({color: 'rgba(8,48,107,' + ((value/maxValue*0.6)+0.2) + ')'}),
    })]
                    };
    return style;
};
