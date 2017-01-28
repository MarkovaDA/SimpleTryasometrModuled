<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tryasometr</title>
        <script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script type="text/javascript">
            ymaps.ready(init);
            //https://tech.yandex.ru/maps/doc/jsapi/2.1/dg/concepts/geoobjects-docpage/
            //https://tech.yandex.ru/maps/jsbox/2.1/geo_object_collection - 
            //пример с добавлением объектов в коллекцию
            var myMap, myPlacemark, myPolyline, HintLayout;
            var labels = ["Маша", "Cаша","Вика", "Наташа", "Лена", "Костя", "Андрей"];
            function getRandom(min, max) {
                return Math.floor((Math.random() * (max - min) + min));
            }
            //моя ближайшая задача -
            //прорисовать все секции, с кружочками на конце,цвет меняется рандомно, с подписями
            //секция регирует на выделение - меняет цвет
            function init() {
                myMap = new ymaps.Map("map", {
                    center: [55.76, 37.64],
                    zoom: 7
                }),
                HintLayout = ymaps.templateLayoutFactory.createClass("<div class='my-hint'>" +
                        "<b>{{ properties.object }}</b><br />" +
                        "{{ properties.address }}" +
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
                        }
                );
                //инициализация метки
                myPlacemark = new ymaps.Placemark([55.76, 37.64],
                        {
                            hintContent: 'Москва!',
                            balloonContent: 'Столица России'
                        });
                //ломаная линия - заменить на массив ломанных линий
                myPolyline = new ymaps.Polyline(
                        [[55.86, 37.84], [55.70, 37.55], [55.8, 37.4]],
                        {   
                            address: "Москва, ул. Зоологическая, 13, стр. 2",
                            object: "Центр современного искусства"
                        },
                        {
                            strokeWidth: 5,
                            strokeColor: '#0000FF',
                            hintLayout: HintLayout
                        }
                );
                //кружочки
                var myCircle = new ymaps.Circle([[55.86, 37.84], 1000], {},
                        {
                            fillColor: "#FFFFFF",
                            strokeWidth: 4,
                            strokeColor: '#FF0000'
                        });
                myMap.geoObjects.add(myPolyline);
                myMap.geoObjects.add(myCircle);
                //события мыши
                myMap.geoObjects.events.add('mouseenter', function (e) {
                    var object = e.get('target');
                    var index = getRandom(0,7);
                    object.properties.set('address', labels[index]);
                    /*object.options.set('fillColor', '#b3b3b3');
                     object.options.set('strokeColor','#808080');
                     object.options.set('strokeWidth', 5);*/
                })
                .add('mouseleave', function (e) {
                });
            }
        </script>
        <style>
           .my-hint {
            display: inline-block;
            padding: 5px;
            height: 35px;
            position: relative;
            left: -10px;
            width: 195px;
            font-size: 11px;
            line-height: 17px;
            color: #333333;
            text-align: center;
            vertical-align: middle;
            background-color: #faefb6;
            border: 1px solid #CDB7B5;
            border-radius: 20px;
            font-family: Arial;
         }
        </style>
    </head>
    <body>
        <h2>tryasometr started</h2>
        <div id="map" style="width: 600px; height: 400px">
            здесь должна быть карта
        </div>
    </body>
</html>
