<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ObjectManager test</title>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src='<c:url value="resources/js/objectmanager.js"/>'></script>
        <style>
            body{
                font-family: Arial;
            }
        </style>
    </head>
    <body>
        <h1>ObjectManager</h1>
        <div id="map" style="width: 640px; height: 480px;">
        </div>
    </body>
</html>
