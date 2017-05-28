<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ObjectManager test</title>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script type="text/javascript" src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src='<c:url value="resources/js/map_legend.js"/>'></script>
        <script type="text/javascript" src='<c:url value="resources/js/simple_routing.js"/>'></script>
        <script type="text/javascript" src='<c:url value="resources/js/loader.js"/>'></script>
        <link rel="stylesheet" href='<c:url value="resources/css/loader.css"/>'></link>
        <style>
            body{
                font-family: Arial;
            }
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
            .customControl {
                background-color: #fff;
                padding: 10px;
                border-radius: 3px;
                max-width: 200px;
                font-size:12px;
                box-shadow: 0 0 10px rgba(0,0,0,0.5);
            }
            .semafore {
                width:15px;
                height:15px;
            }
            .green {
                border-radius: 50%;
                background: greenyellow;
            }
            .yellow {
                border-radius: 50%;
                background: yellow;
            }
            .red {
                border-radius: 50%;
                background: red;
            }
            .alert_red {
                border-radius: 50%;
                background:darkred;
            }
        </style>
    </head>
    <body>
        <h1>ObjectManager</h1>
        <div id="loader"></div>
        
        <div id="map" style="width: 640px; height: 480px;">
        </div>
        <div class="customControl">
            <ins>Условные обозначения</ins>
            <table>
                <tr>
                    <td><div class="green semafore"></div></td>
                    <td> - отлично</td>
                </tr>
                <tr>
                    <td><div class="yellow semafore"></div></td>
                    <td> - хорошо</td>
                </tr>
                <tr>
                    <td><div class="red semafore"></div></td>
                    <td> - плохо</td>
                </tr>
                <tr>
                    <td><div class="alert_red semafore"></div></td>
                    <td> - очень плохо</td>
                </tr>
            </table>
        </div>
    </body>
</html>
