ymaps.ready(function () {
     console.log("карта загружена");
    var showSectionsButton = new ymaps.control.Button({
        data: { content: "Отобразить секции" },
        options: { selectOnClick: true }
    });
    var showMarshrutButton = new ymaps.control.Button({
        data: { content: "Получить маршруты" },
        options: { selectOnClick: true }
    });
    var map = new ymaps.Map('map', {
        center: [51.67, 39.18],
        zoom: 15,
        controls: [showSectionsButton, showMarshrutButton,'zoomControl','fullscreenControl']
    }, {
        buttonMaxWidth: 300
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
    
    var from, to, multiRoute, relocatedFrom = false;    
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
    //отображаем все секции по нажатию на соответсвующую кнопку
    showSectionsButton.events.add('select', function () {
        $.getJSON('object_manager/get_features')
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
        });
    });
    //очищаем карту от секций
    showSectionsButton.events.add('deselect', function () {
        map.geoObjects.removeAll();
    });
    //отображение маршрута между двумя точками
    showMarshrutButton.events.add('select', function() {
        multiRoute = new ymaps.multiRouter.MultiRoute({
        // Описание опорных точек мультимаршрута
        referencePoints: [
           from.geometry.getCoordinates(),
           to.geometry.getCoordinates()
        ],
        //параметры маршрутизации
        params: {
            //Ограничение на максимальное количество маршрутов, возвращаемое маршрутизатором.
            results: 5
        }
        }, {
            //Автоматически устанавливать границы карты так, чтобы маршрут был виден целиком.
            boundsAutoApply: true
        });
        map.geoObjects.add(multiRoute);
    });
    showMarshrutButton.events.add('deselect',function(){
        map.geoObjects.remove(multiRoute);
    });
});



