 ymaps.ready(function () {
 // Создаем собственный класс.
CustomControlClass = function (options) {
    CustomControlClass.superclass.constructor.call(this, options);
    this._$content = null;
    this._geocoderDeferred = null;
};
//и наследуем его от collection.Item.
ymaps.util.augment(CustomControlClass, ymaps.collection.Item, {
    onAddToMap: function (map) {
        CustomControlClass.superclass.onAddToMap.call(this, map);
        this._lastCenter = null;
        this.getParent().getChildElement(this).then(this._onGetChildElement, this);
    },

    onRemoveFromMap: function (oldMap) {
        this._lastCenter = null;
        if (this._$content) {
            this._$content.remove();
            this._mapEventGroup.removeAll();
        }
        CustomControlClass.superclass.onRemoveFromMap.call(this, oldMap);
    },

    _onGetChildElement: function (parentDomContainer) {
        // Создаем HTML-элемент с текстом.
        this._$content = $('.customControl').appendTo(parentDomContainer);
        this._mapEventGroup = this.getMap().events.group();
        // Запрашиваем данные после изменения положения карты.
        this._mapEventGroup.add('boundschange', this._createRequest, this);
        // Сразу же запрашиваем название места.
        this._createRequest();
    },

    _createRequest: function() {
        /*var lastCenter = this._lastCenter = this.getMap().getCenter().join(',');
        // Запрашиваем информацию о месте по координатам центра карты.
        ymaps.geocode(this._lastCenter, {
            // Указываем, что ответ должен быть в формате JSON.
            json: true,
            // Устанавливаем лимит на кол-во записей в ответе.
            results: 1
        }).then(function (result) {
                // Будем обрабатывать только ответ от последнего запроса.
                if (lastCenter == this._lastCenter) {
                    this._onServerResponse(result);
                }
            }, this);*/
    },

    _onServerResponse: function (result) {
        /*// Данные от сервера были получены и теперь их необходимо отобразить.
        // Описание ответа в формате JSON.
        var members = result.GeoObjectCollection.featureMember,
            geoObjectData = (members && members.length) ? members[0].GeoObject : null;
        if (geoObjectData) {
            this._$content.text(geoObjectData.metaDataProperty.GeocoderMetaData.text);
        }*/
    }
});

});

