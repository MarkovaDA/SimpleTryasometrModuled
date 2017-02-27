ymaps.ready(function () {
    var showSectionsButton = new ymaps.control.Button({
        data: {content: "Отобразить секции"},
        options: {selectOnClick: true}
    });
    var showMarshrutButton = new ymaps.control.Button({
        data: {content: "Получить маршруты"},
        options: {selectOnClick: true}
    });
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15,
        controls: [showSectionsButton, showMarshrutButton, 'zoomControl', 'fullscreenControl']
    }, {
        buttonMaxWidth: 300
    }),
    objectManager = new ymaps.ObjectManager();
    rectObjectManager = new ymaps.ObjectManager();
    basePointsManager = new ymaps.ObjectManager();
    //всплывающая подсказка
    var HintLayout = ymaps.templateLayoutFactory.createClass("<div class='my-hint'>" +
            "<b>Оценка качества</b><br />" +
            "{{properties.sectionId}}" +
            "{{properties.rectLength}}" +
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

    var from, to, relocatedFrom = false;
    map.events.add('click', function (e) {
        var coords = e.get('coords');
        if (from && to) {
            if (!relocatedFrom) {
                from.geometry.setCoordinates(coords);
                relocatedFrom = true;
            } else {
                to.geometry.setCoordinates(coords);
                relocatedFrom = false;
            }
        }
        if (!from) {
            from = new ymaps.Placemark(coords, {
            }, {
                preset: 'islands#icon',
                iconColor: '#f80000'
            });
            map.geoObjects.add(from);
        } else if (!to) {
            to = new ymaps.Placemark(coords, {
            }, {
                preset: 'islands#icon',
                iconColor: '#0095b6'
            });
            map.geoObjects.add(to);
        }
    });
    objectManager.objects.events.add(['mouseenter', 'mouseleave'], onSectionHover);

    function onSectionHover(e) {
        var objectId = e.get('objectId');
        if (e.get('type') === 'mouseenter') {
            objectManager.objects.setObjectOptions(objectId, {
                opacity: 0.5
            });
        } else {
            objectManager.objects.setObjectOptions(objectId, {
                opacity: 1
            });
        }
    }
    //отображаем все секции по нажатию на соответсвующую кнопку
    showSectionsButton.events.add('select', function () {
        $('#loader').fadeIn(100);
        $.getJSON('object_manager/get_features')
                .done(function (geoJson) {
                    console.log("geoJson is ready");
                    geoJson.features.filter(function (feature) {
                        if (feature.geometry.type === "Circle") {
                            feature.geometry.coordinates = feature.geometry.coordinates[0];
                        }
                    });
                    objectManager.removeAll();
                    objectManager.add(geoJson);
                    objectManager.objects.options.set({
                        hintLayout: HintLayout
                    });
                    map.geoObjects.add(objectManager);
                    $('#loader').fadeOut(100);
                });
    });
    //очищаем карту от секций
    showSectionsButton.events.add('deselect', function () {
        map.geoObjects.removeAll();
    });
    //ОТОБРАЖЕНИЕ МАРШРУТА МЕЖДУ ДВУМЯ ТОЧКАМИ
    showMarshrutButton.events.add('select', function () {
        /*ymaps.route([from.geometry.getCoordinates(), 
                    to.geometry.getCoordinates()], 
                    { multiRoute: true })
        .done(function (multiRoute) {
            map.geoObjects.add(multiRoute);
            multiRoute.editor.start({
                addWayPoints: true,
                removeWayPoints: true
            });
            multiRoute.editor.stop();
        });*/
        var multiRoute = new ymaps.multiRouter.MultiRoute({
            referencePoints: [
                from.geometry.getCoordinates(),
                to.geometry.getCoordinates()
            ]
            /*params: {
                routingMode: 'masstransit'
            }*/
        });
        multiRoute.model.events
            .add("requestsuccess", function (event) {
                var routes = event.get("target").getRoutes();
                for(var i=0; i < routes.length; i++){
                    //массив всех точек
                    console.log((routes[i].getPaths()).length);
                }
                //console.log(routes.length);
            })
            .add("requestfail",function(event){
                console.log("Error multirouting: " + event.get("error").message);
            });
        multiRoute.events.add("update", function(){
            multiRoute.getRoutes().each(function(route){
                multiRoute.setActiveRoute(route);
            });
        });
        //событие обработки смены активного маршрута
        var counter = 0;
        multiRoute.events.add('activeroutechange',function(){
            console.log("active route change");
            var paths = new Array();
            var currentPath = new Object();
                currentPath.id = ++counter;
                currentPath.segments = new Array();
            multiRoute.getActiveRoute().getPaths().each(function(path){
                path.getSegments().each(function(segment) {
                    var currentSegment = new Object();
                    currentSegment.points = segment.geometry._coordPath._coordinates;
                    currentPath.segments.push(currentSegment);
                });
                paths.push(currentPath);
            });
            console.log(currentPath);
            //console.log(multiRoute.getActiveRoute().getPaths().get(0).getSegments().get(0).geometry._coordPath._coordinates);
        });
       
        map.geoObjects.add(multiRoute);
        
        showMarshrutButton.events.add('deselect', function () {
            //map.geoObjects.remove(multiRoute);
            map.geoObjects.splice(2, map.geoObjects.getLength() - 2);
        });
    });
});





