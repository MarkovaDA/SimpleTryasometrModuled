<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>LoadingObjectManager test</title>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
        <script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
        <script type="text/javascript" src='<c:url value="resources/js/loadingobjectmanager.js"/>'></script>
        <title>Loading Object Manager</title>
    </head>
    <body>
        <h1>Loading Object Manager</h1>
        <div id="map" style="width: 640px; height: 480px;">
        </div>
    </body>
</html>
