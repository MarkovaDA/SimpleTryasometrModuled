        var map;
        var sourceVector = new ol.source.Vector();
        var rectSourceVector = new ol.source.Vector(); //для отображения прямоугольников первых секций
        
        var lineStyle = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#f80000',
                        width: 2
                    })
        });
        
        
        
        var rectStyle1 = new ol.style.Style({
            stroke: new ol.style.Stroke({
                        color: 'black',
                        width: 2
                    })
            /*image: new ol.style.Circle({
                    radius: 2,
                    stroke: new ol.style.Stroke({
                      width: 3,
                      color: 'green'
                    }),
                    fill: new ol.style.Fill({
                      color: 'green'
                    })
            })*/
        });
        var rectStyle2 = new ol.style.Style({
            stroke: new ol.style.Stroke({
                        color: 'green',
                        width: 2
                    })
        });
        var rectStyle3 = new ol.style.Style({
            stroke: new ol.style.Stroke({
                        color: '#8b00ff',
                        width: 2
                    })
        });
        
        var stylesRect = new Array(); //массив стилей для сегментов
        stylesRect[0] = rectStyle1; stylesRect[1] = rectStyle2; stylesRect[2] = rectStyle3;
          
        var layerVector = new ol.layer.Vector({
                source: sourceVector,
                style: lineStyle
        });
        
        //слой для нанесения точек на карту
        var rectLayerVector = new ol.layer.Vector({
            source: rectSourceVector,
            style: rectStyle1                   
        });
        //точка на углах прямоугольника
        function addNewRectFeature(point, index){
            //передаю одну единственную точку
            var rectOnePoint = new ol.geom.LineString(point);
                rectOnePoint.transform('EPSG:4326', 'EPSG:3857');                
            var feature = new ol.Feature({
                    geometry: rectOnePoint
            });
            index = parseInt(index);
            //!!!что-то массив не распознает
            if (index === 0){
                feature.setStyle(rectStyle1);
            }
            if (index === 1){
                feature.setStyle(rectStyle2);
            }
            if (index === 2){
               feature.setStyle(rectStyle3); 
            }
            rectSourceVector.addFeature(feature);
        }
        //новый отрезок     
        function addNewFeature(points){
            var lineString = new ol.geom.LineString(points);                
                lineString.transform('EPSG:4326', 'EPSG:3857');
            var feature = new ol.Feature({
                    geometry: lineString
            });
            sourceVector.addFeature(feature);
        }
        function getVectorLayer(coordinates){
            
            var lineString = new ol.geom.LineString(coordinates); //создаём отрезок                
                lineString.transform('EPSG:4326', 'EPSG:3857'); 
            
            var feature = new ol.Feature({
                    geometry: lineString,
                    name: 'Line'
            }); 
            var styles = [];
            
            var lineStyle = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#f80000',
                        width: 3
                    })
            });           
            styles.push(lineStyle);  
            //создаём в отдельном слое
            var source = new ol.source.Vector({
                features: [feature]
            });            
            //переделать путем добавления нескольких features
            return new ol.layer.Vector({
                    source: source,
                    style: styleFunction
            });
        }    
        var styleFunction = function(feature) {
            var geometry = feature.getGeometry();
            var styles = [
              // linestring
              new ol.style.Style({
                stroke: new ol.style.Stroke({
                  color: 'red',
                  width: 5
                })
              })
            ];
            //для каждого сегмента функция
            geometry.forEachSegment(function(start, end) {
                styles.push(new ol.style.Style({
                        geometry: new ol.geom.Point(start),
                        image: new ol.style.Circle({
                            radius: 2,
                            stroke: new ol.style.Stroke({
                              width: 1,
                              color: 'blue'
                            }),
                            fill: new ol.style.Fill({
                              color: 'blue'
                            })
                        })
                }));
            });
            return styles;
      };
        
        $(document).ready(function(){           
                map = new ol.Map({
                    layers: [
                      new ol.layer.Tile({
                        source: new ol.source.OSM()
                      })
                      //,vector
                    ],
                    target: 'map',
                    controls: ol.control.defaults({
                      attributionOptions:  ({
                        collapsible: false
                      })
                    }),
                    view: new ol.View({
                      center: [39.1841, 51.67202],
                      zoom: 15
                    })
                });
                var voronezhLon = 39.1841;
                var voronezhLat = 51.67202;
                map.getView().setCenter(ol.proj.transform([voronezhLon, voronezhLat], 'EPSG:4326', 'EPSG:3857'));
        });



