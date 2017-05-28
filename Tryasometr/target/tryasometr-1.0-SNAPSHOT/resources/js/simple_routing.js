ymaps.ready(function () {
    //определяем две кнопки интерфейса
    var showSectionsButton = new ymaps.control.Button({
        data: {content: "Отобразить секции"},
        options: {selectOnClick: true}
    });
    var showMarshrutButton = new ymaps.control.Button({
        data: {content: "Получить маршруты"},
        options: {selectOnClick: true}
    });
    //определяем виджет
    var customControl = new CustomControlClass();
    //определяем карту
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15,
        controls: [showSectionsButton, showMarshrutButton, 'zoomControl']
    }, {
        buttonMaxWidth: 300
    }),
    
    objectManager = new ymaps.ObjectManager();//структура для рисования секций 
    rectObjectManager = new ymaps.ObjectManager(); //структура для прорисовки прямоугольников аппроксимирующих
    basePointsManager = new ymaps.ObjectManager();//структура для отрисовки базовых точек
    //всплывающая подсказка
    var HintLayout = ymaps.templateLayoutFactory.createClass("<div class='my-hint'>" +
            "<b>Оценка степени неровности</b><br />" +
            "{{properties.sectionValue}}" +
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
    //ручная настройка элемента fullscreencontrol
    map.controls.add(new ymaps.control.FullscreenControl({options: { position: { right: 10, bottom: 25 }}}));    
    //установка пользовательского виджета легенды карты
    map.controls.add(customControl, {
        float: 'none',
        position: {
            top: 10,
            right: 10
        }
    });
    var from, to, relocatedFrom = false;
    //установка точек на карту
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
    //декоративное изменение прозрачности секции при наведении мыши
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
    //отображение маршрута между двумя точками
    showMarshrutButton.events.add('select', function () {
        //$('#loader').fadeIn(100);
        ymaps.route([
            //передаем конечную и начальную точку маршрута
            from.geometry.getCoordinates(),
            to.geometry.getCoordinates()
        ])
        .then(function (route) {
                map.geoObjects.add(route);
                var paths = new Array();
                //собираем маршрутный объект для отправки на сервер
                for (var i = 0; i < route.getPaths().getLength(); i++) {
                    var currentPath = new Object();
                    currentPath.id = i;
                    currentPath.segments = new Array();
                    var way = route.getPaths().get(i);
                    var segments = way.getSegments();
                    for (var j = 0; j < segments.length; j++) {
                        var currentSegment = new Object();
                        var coordinates = segments[j].getCoordinates();
                        currentSegment.points = coordinates;
                        currentPath.segments.push(currentSegment);
                    }
                    paths.push(currentPath);
                    console.log(paths);
                }
                //отображение аппроксимирующих секций
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    'type': 'POST',
                    'url': 'put_yandex_points',
                    'data': JSON.stringify(paths),
                    'dataType': 'json',
                    'success': function (data) {
                        data.features.filter(function (feature) {
                            if (feature.geometry.type === "Circle") {
                                feature.geometry.coordinates = feature.geometry.coordinates[0];
                            }
                        });
                        console.log(data);
                        console.log('drawed sections');
                        objectManager.removeAll();
                        objectManager.add(data);
                        objectManager.objects.options.set({
                            hintLayout: HintLayout
                        });
                        map.geoObjects.add(objectManager);
                        $('#loader').fadeOut(100);
                    }
                });
                //аппроксимирующие прямоугольники
                /*.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    'type': 'POST',
                    'url': 'draw_rectangles',
                    'data': JSON.stringify(paths),
                    'dataType': 'json',
                    'success': function (data) {
                        console.log("drawed features");
                        console.log(data);
                        rectObjectManager.removeAll();
                        rectObjectManager.add(data);
                        map.geoObjects.add(rectObjectManager);
                    }
                });*/
            });      
    });
    //удаляем маркеры, установленные на карту
    showMarshrutButton.events.add('deselect', function () {
            //map.geoObjects.remove(multiRoute);
        map.geoObjects.splice(2, map.geoObjects.getLength() - 2);
    });
});



