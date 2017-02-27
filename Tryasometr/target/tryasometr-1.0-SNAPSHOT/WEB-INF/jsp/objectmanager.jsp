<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ObjectManager test</title>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script type="text/javascript" src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src='<c:url value="resources/js/multi_routing.js"/>'></script>
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
        </style>
    </head>
    <body>
        <h1>ObjectManager</h1>
        <div id="loader"></div>
        <div id="map" style="width: 640px; height: 480px;">
        </div>
    </body>
</html>
