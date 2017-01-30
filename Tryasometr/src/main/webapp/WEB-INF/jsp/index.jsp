<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tryasometr</title>
        <script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script type="text/javascript">
            var myMap, HintLayout;
        
            ymaps.ready(function(){
                  $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    'type': 'GET',
                    'url': 'get_sections',
                    'success': function (sections) {             
                        console.log(sections);
                        myMap = new ymaps.Map("map", {
                            center: [51.67, 39.18],
                            zoom: 10
                        });
                        HintLayout = ymaps.templateLayoutFactory.createClass("<div class='my-hint'>" +
                        "<b>{{ properties.object }}</b><br />" +
                        "{{ properties.value }}" +
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
                        for (var i = 0, l = sections.length; i < l; i++) {
                            var coords = new Array(); //координаты текущей секции
                            coords.push([sections[i].lat1, sections[i].lon1]);
                            coords.push([sections[i].lat2, sections[i].lon2]);
                            coords.push([sections[i].lat3, sections[i].lon3]);
                            coords.push([sections[i].lat4, sections[i].lon4]); 
                            var currentPolygon = new ymaps.Polyline(
                                coords,
                                {
                                    value:sections[i].seсtionID,
                                    object: "Качество"
                                },
                                {
                                    strokeWidth: 10,
                                    strokeColor: '#00b300',
                                    hintLayout: HintLayout
                                }
                            );
                            myMap.geoObjects.add(currentPolygon);
                            for(var j=0; j<4;j++){
                                var myCircle = new ymaps.Circle([coords[j], 15], {},
                                {
                                    fillColor: "#FFFFFF",
                                    strokeWidth: 4,
                                    strokeColor: '#ff0000'
                                });
                                myMap.geoObjects.add(myCircle);
                            }
                        }
                        //привязка событий
                        myMap.geoObjects.events.add('mouseenter', function (e) {
                            //привязка событий
                            /*var object = e.get('target');
                            var index = getRandom(0,7);
                            object.properties.set('address', labels[index]);
                            object.options.set('fillColor', '#b3b3b3');
                            object.options.set('strokeColor','#808080');
                            object.options.set('strokeWidth', 5);*/
                        })
                        .add('mouseleave', function (e) {
                        });
                    }
                });
            });
            function getRandom(min, max) {
                return Math.floor((Math.random() * (max - min) + min));
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
