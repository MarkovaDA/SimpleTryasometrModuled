ymaps.ready(function () {
    console.log("карта загружена");
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15
    }),
    objectManager = new ymaps.ObjectManager();

    $.getJSON('object_manager/get_features')
        .done(function (geoJson) {
            console.log("geoJson is ready");
            geoJson.features.filter(function(feature){
                if (feature.geometry.type === "Circle"){
                    feature.geometry.coordinates = feature.geometry.coordinates[0];
                }
            });
            objectManager.add(geoJson);
            map.geoObjects.add(objectManager);
        });
});


