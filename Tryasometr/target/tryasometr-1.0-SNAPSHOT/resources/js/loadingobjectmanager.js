
ymaps.ready(function(){
    console.log("карта загружена");
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15
    }),
    
    loadingObjectManager = new ymaps.LoadingObjectManager('loading_object_manager/bounds?bbox=%b',
    {   
        clusterize: true,
        //paddingTemplate: 'responseObject'
        //paddingParamName: "callbackOption"
        clusterHasBalloon: false,
        geoObjectOpenBalloonOnClick: false
    });
    map.geoObjects.add(loadingObjectManager);
});


