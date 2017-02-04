ymaps.ready(function () {
    console.log("карта загружена");
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15
    }),
    objectManager = new ymaps.ObjectManager();
    //всплывающая подсказка
    var HintLayout = ymaps.templateLayoutFactory.createClass("<div class='my-hint'>" +
        "<b>Оценка качества</b><br />" +
        "{{properties.sectionId}}" +
        "</div>", {
            getShape: function () {
                var el = this.getElement(),
                        result = null;
                if (el) {
                    var firstChild = el.firstChild;
                    result = new ymaps.shape.Rectangle(
                            new ymaps.geometry.pixel.Rectangle([
                                [0, 0],
                                [firstChild.offsetWidth, firstChild.offsetHeight]
                            ])
                            );
                }
                return result;
            }
    });
    
    //пока что функцию получения данных отключим
    /*$.getJSON('object_manager/get_features')
        .done(function (geoJson) {
            console.log("geoJson is ready");
            geoJson.features.filter(function(feature){
                if (feature.geometry.type === "Circle"){
                    feature.geometry.coordinates = feature.geometry.coordinates[0];
                }
            });
            objectManager.add(geoJson);
            objectManager.objects.options.set({
                hintLayout: HintLayout
            });
            map.geoObjects.add(objectManager);
    });*/
    //сделать кнопку - показать все секции
    var from, to, relocatedFrom = false;
    
    map.events.add('click', function(e){
        var coords = e.get('coords');
        if (from && to){
            if (!relocatedFrom){
                from.geometry.setCoordinates(coords);
                relocatedFrom = true;
            }
            else {
                to.geometry.setCoordinates(coords);
                relocatedFrom = false;
            }
        }
        if (!from){
            from =  new ymaps.Placemark(coords, {
                }, {
                    preset: 'islands#icon',
                    iconColor: '#f80000'
                });
            map.geoObjects.add(from);
        }
        else if (!to){
            to =  new ymaps.Placemark(coords, {
                }, {
                    preset: 'islands#icon',
                    iconColor: '#0095b6'
                });
            map.geoObjects.add(to);
        }
        
    });
    objectManager.objects.events.add(['mouseenter', 'mouseleave'], onSectionHover);
    //приделать хинт еще с надписью
    function onSectionHover (e) {
        var objectId = e.get('objectId');
        if (e.get('type') === 'mouseenter'){
            objectManager.objects.setObjectOptions(objectId, {
                opacity: 0.5
            });
        }
        else {
           objectManager.objects.setObjectOptions(objectId, {
                opacity: 1
           });
        }
    }
 
});



